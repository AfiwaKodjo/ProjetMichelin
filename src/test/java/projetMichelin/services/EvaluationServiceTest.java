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
import projetMichelin.dto.evaluation.EvaluationUpdateDto;
import projetMichelin.entity.EvaluationEntity;
import projetMichelin.entity.RestaurantEntity;
import projetMichelin.exceptions.NotFoundException;
import projetMichelin.repository.EvaluationRepository;
import projetMichelin.repository.S3Repository;
import projetMichelin.services.indexation.IndexService;

//tests du service des évaluations
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

    //évaluation 10 de Lucien dans le restaurant 1
    private EvaluationEntity evaluationLucien() {
        return EvaluationEntity.builder()
                .id(10).reviewerName("Lucien Bramard").comment("meilleures lasagnes").note(3)
                .restaurant(restaurant()).build();
    }

    // création
    @Test
    void creerEvaluation() {
        when(restaurantService.findRestaurant(1)).thenReturn(restaurant());
        when(evaluationRepository.save(any(EvaluationEntity.class))).thenAnswer(invocation -> {
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
    void creerRestaurantInexistant() {
        when(restaurantService.findRestaurant(999)).thenThrow(new NotFoundException("introuvable"));

        assertThrows(NotFoundException.class, () -> evaluationService.createEvaluation(999,
                new EvaluationCreateRequestDto("sushi frais", 3), "Lucien Bramard"));
        verify(evaluationRepository, never()).save(any());
        verify(indexService, never()).indexComment(any(), any());
    }

    // suppression
    @Test
    void supprimerParAuteur() {
        EvaluationEntity evaluation = evaluationLucien();
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluation));

        evaluationService.deleteEvaluation(1, 10, "Lucien Bramard", false);

        verify(evaluationRepository).delete(evaluation);
        verify(indexService).deleteComment(10);
    }

    @Test
    void supprimerParAutreUser() {
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluationLucien()));

        assertThrows(AccessDeniedException.class,
                () -> evaluationService.deleteEvaluation(1, 10, "Quelqu'un d'autre", false));
        verify(evaluationRepository, never()).delete(any());
    }

    @Test
    void supprimerParAdmin() {
        EvaluationEntity evaluation = evaluationLucien();
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluation));

        evaluationService.deleteEvaluation(1, 10, "Noël Flantier", true);

        verify(evaluationRepository).delete(evaluation);
    }

    @Test
    void supprimerMauvaisRestaurant() {
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluationLucien()));

        //l'évaluation 10 est dans le restaurant 1, pas le 2
        assertThrows(NotFoundException.class,
                () -> evaluationService.deleteEvaluation(2, 10, "Lucien Bramard", false));
        verify(evaluationRepository, never()).delete(any());
    }

    @Test
    void supprimerInexistante() {
        when(evaluationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> evaluationService.deleteEvaluation(1, 999, "Lucien Bramard", false));
    }

    // mes évaluations
    @Test
    void mesEvaluations() {
        when(evaluationRepository.findByReviewerName("Lucien Bramard"))
                .thenReturn(List.of(evaluationLucien()));

        List<EvaluationResponseDto> resultat = evaluationService.getEvaluations("Lucien Bramard");

        assertEquals(1, resultat.size());
        assertEquals("Lucien Bramard", resultat.get(0).getReviewerName());
    }

    // recherche
    @Test
    void rechercheTrouve() {
        when(indexService.searchComment("lasagnes")).thenReturn(List.of(10));
        when(evaluationRepository.findAllById(List.of(10))).thenReturn(List.of(evaluationLucien()));

        List<EvaluationResponseDto> resultat = evaluationService.searchEvaluations("lasagnes");

        assertEquals(1, resultat.size());
        assertEquals("meilleures lasagnes", resultat.get(0).getComment());
    }

    @Test
    void rechercheVide() {
        when(indexService.searchComment("pizza")).thenReturn(List.of());
        when(evaluationRepository.findAllById(List.of())).thenReturn(List.of());

        assertTrue(evaluationService.searchEvaluations("pizza").isEmpty());
    }

    // modification

    @Test
    void modifierParAuteur() {
        EvaluationEntity evaluation = evaluationLucien();
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        EvaluationResponseDto resultat = evaluationService.updateEvaluation(1, 10,new EvaluationUpdateDto("lasagnes un peu froides", 1), "Lucien Bramard", false);

        assertEquals("lasagnes un peu froides", resultat.getComment());
        assertEquals(1, resultat.getNote());
        //l'index doit avoir le nouveau commentaire
        verify(indexService).deleteComment(10);
        verify(indexService).indexComment(10, "lasagnes un peu froides");
    }

    @Test
    void modifierParAutreUser() {
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluationLucien()));

        assertThrows(AccessDeniedException.class, () -> evaluationService.updateEvaluation(1, 10,
                new EvaluationUpdateDto("pirate", 0), "Quelqu'un d'autre", false));
        verify(evaluationRepository, never()).save(any());
    }

    @Test
    void modifierParAdmin() {
        EvaluationEntity evaluation = evaluationLucien();
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(evaluation)).thenReturn(evaluation);

        EvaluationResponseDto resultat = evaluationService.updateEvaluation(1, 10,
                new EvaluationUpdateDto("modifié par l'admin", 2), "Noël Flantier", true);

        assertEquals("modifié par l'admin", resultat.getComment());
    }

    @Test
    void modifierMauvaisRestaurant() {
        when(evaluationRepository.findById(10)).thenReturn(Optional.of(evaluationLucien()));

        assertThrows(NotFoundException.class, () -> evaluationService.updateEvaluation(2, 10,
                new EvaluationUpdateDto("x", 1), "Lucien Bramard", false));
        verify(evaluationRepository, never()).save(any());
    }
}