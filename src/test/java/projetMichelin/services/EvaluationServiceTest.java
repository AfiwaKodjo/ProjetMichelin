package projetMichelin.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import projetMichelin.dto.evaluation.EvaluationCreateRequestDto;
import projetMichelin.dto.evaluation.EvaluationResponseDto;
import projetMichelin.entity.EvaluationEntity;
import projetMichelin.entity.RestaurantEntity;
import projetMichelin.exceptions.NotFoundException;
import projetMichelin.repository.EvaluationRepository;
import projetMichelin.repository.S3Repository;
import projetMichelin.services.indexation.IndexService;

//Tests unitaires pour le service des évaluations.
@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private S3Repository s3Repository;

    @Mock
    private IndexService indexService;

    @InjectMocks
    private EvaluationService evaluationService;

    private RestaurantEntity restaurant() {
        return RestaurantEntity.builder().id(1).name("Le Bistrot").address("Nancy").build();
    }

    private EvaluationEntity evaluationDeLucien() {
        return EvaluationEntity.builder()
                .id(10).reviewerName("Lucien Bramard").comment("meilleures lasagnes").note(3)
                  .restaurant(restaurant()).build();
    }


    @Test
    void createEvaluation_enregistreAvecLAuteurDuTokenEtIndexe() {
        when(restaurantService.findRestaurant(1)).thenReturn(restaurant());
        when(evaluationRepository.save(any(EvaluationEntity.class)))
        .thenAnswer(invocation -> {
            EvaluationEntity e = invocation.getArgument(0);
            e.setId(10);
            return e;
        });

        EvaluationResponseDto resultat = evaluationService.createEvaluation(1,
                new EvaluationCreateRequestDto("meilleures lasagnes", 3), "Lucien Bramard");

        assertEquals("Lucien Bramard", resultat.getReviewerName());
        assertEquals(3, resultat.getNote());
        verify(indexService).indexComment(10, "meilleures lasagnes"); 
    }

    @Test
    void createEvaluation_restaurantInexistant_leveNotFoundEtNEnregistreRien() {
        when(restaurantService.findRestaurant(999)).thenThrow(new NotFoundException("introuvable"));

        assertThrows(NotFoundException.class, () -> evaluationService.createEvaluation(999,
                new EvaluationCreateRequestDto("Superbe homard", 3), "Lucien Bramard"));
        verify(evaluationRepository, never()).save(any());
        verify(indexService, never()).indexComment(any(), any());
    }

    @Test
    void deleteEvaluation_parSonAuteur_supprimeEtRetireDeLIndex() {
        EvaluationEntity evaluation = evaluationDeLucien();
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluation));

        evaluationService.deleteEvaluation(1, 10, "Lucien Bramard", false);

        verify(evaluationRepository).delete(evaluation);
        verify(indexService).deleteComment(10);
    }

    @Test
    void deleteEvaluation_parUnAutreUser_leveAccessDenied() {
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluationDeLucien()));
        assertThrows(AccessDeniedException.class,
                () -> evaluationService.deleteEvaluation(1, 10, "Quelqu'un d'autre", false));
        verify(evaluationRepository, never()).delete(any());
    }

    @Test
    void deleteEvaluation_parUnAdmin_supprimeMemeSiCeNestPasLaSienne() {
        EvaluationEntity evaluation = evaluationDeLucien();
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluation));

        evaluationService.deleteEvaluation(1, 10, "Noël Flantier", true);

        verify(evaluationRepository).delete(evaluation);
    }

    @Test
    void deleteEvaluation_mauvaisRestaurant_leveNotFound() {
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluationDeLucien()));

        // l'évaluation 10 est dans le restaurant 1, pas le 2
        assertThrows(NotFoundException.class,
                () -> evaluationService.deleteEvaluation(2, 10, "Lucien Bramard", false));
        verify(evaluationRepository, never()).delete(any());
    }

    @Test
    void deleteEvaluation_inexistante_leveNotFound() {
        when(evaluationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> evaluationService.deleteEvaluation(1, 999, "Lucien Bramard", false));
    }

    @Test
    void getEvaluations_renvoieCellesDeLUtilisateur() {
        when(evaluationRepository.findByReviewerName("Lucien Bramard"))
                .thenReturn(List.of(evaluationDeLucien()));

        List<EvaluationResponseDto> resultat = evaluationService.getEvaluations("Lucien Bramard");

        assertEquals(1, resultat.size());
        assertEquals("Lucien Bramard", resultat.get(0).getReviewerName());
    }

    @Test
    void searchEvaluations_renvoieLesEvaluationsTrouveesParLucene() {
        when(indexService.searchComment("homard")).thenReturn(List.of(10));
        when(evaluationRepository.findAllById(List.of(10))).thenReturn(List.of(evaluationDeLucien()));

        List<EvaluationResponseDto> resultat = evaluationService.searchEvaluations("homard");

        assertEquals(1, resultat.size());
        assertEquals("meilleures lasagnes", resultat.get(0).getComment());
    }

    @Test
    void searchEvaluations_aucunResultat_listeVide() {
        when(indexService.searchComment("pizza")).thenReturn(List.of());
        when(evaluationRepository.findAllById(List.of())).thenReturn(List.of());

        assertTrue(evaluationService.searchEvaluations("pizza").isEmpty());
    }
}