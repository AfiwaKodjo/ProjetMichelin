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

import projetMichelin.dto.restaurant.RestaurantCreateRequestDto;
import projetMichelin.dto.restaurant.RestaurantResponseDto;
import projetMichelin.dto.restaurant.RestaurantUpdateDto;
import projetMichelin.entity.EvaluationEntity;
import projetMichelin.entity.RestaurantEntity;
import projetMichelin.exceptions.NotFoundException;
import projetMichelin.repository.RestaurantRepository;
import projetMichelin.repository.S3Repository;

//tests unitaires du service des restaurants.
@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private S3Repository s3Repository;

    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void restaurantSansEvaluation_moyenneVautMoinsUn() {
        RestaurantEntity restaurant = RestaurantEntity.builder()
            .id(1).name("ours noir").address("Nancy").build(); // liste d'évaluations vide
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        RestaurantResponseDto resultat = restaurantService.getRestaurantById(1);

        assertEquals(-1, resultat.getMoyenne());
    }

    @Test
    void restaurantAvecEvaluations_moyenneCalculee() {
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .id(1).name("Le Bistrot").address("Nancy").build();
        restaurant.getEvaluations().add(EvaluationEntity.builder().note(3).build());
        restaurant.getEvaluations().add(EvaluationEntity.builder().note(1).build());
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        RestaurantResponseDto resultat = restaurantService.getRestaurantById(1);

        assertEquals(2.0, resultat.getMoyenne());
    }

    @Test
    void restaurantAvecUneSeuleEvaluation_moyenneEgaleALaNote() {
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .id(1).name("Le Bistrot").address("Nancy").build();
        restaurant.getEvaluations().add(EvaluationEntity.builder().note(0).build());
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        assertEquals(0.0, restaurantService.getRestaurantById(1).getMoyenne());
    }

    @Test
    void getRestaurants_renvoieTousLesRestaurants() {
        when(restaurantRepository.findAll()).thenReturn(List.of(
                RestaurantEntity.builder().id(1).name("A").address("Nancy").build(),
                RestaurantEntity.builder().id(2).name("B").address("Rouen").build()));
        List<RestaurantResponseDto> resultat = restaurantService.getRestaurants();

        assertEquals(2, resultat.size());
    }

    @Test
    void getRestaurants_aucunRestaurant_listeVide() {
        when(restaurantRepository.findAll()).thenReturn(List.of());

        assertTrue(restaurantService.getRestaurants().isEmpty());
    }

    @Test
    void getRestaurantById_inexistant_leveNotFound() {
        when(restaurantRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> restaurantService.getRestaurantById(999));
    }

    @Test
    void createRestaurant_sauvegardeEtRenvoieLeRestaurant() {
        when(restaurantRepository.save(any(RestaurantEntity.class))).thenAnswer(invocation -> {
            RestaurantEntity r = invocation.getArgument(0);
            r.setId(7);
            return r;
        });

        RestaurantResponseDto resultat = restaurantService.createRestaurant(
                new RestaurantCreateRequestDto("sushi", "Nancy"));

        assertEquals(7, resultat.getId());
        assertEquals("sushi", resultat.getName());
        assertEquals(-1, resultat.getMoyenne());
        verify(restaurantRepository, times(1)).save(any(RestaurantEntity.class));
    }

    @Test
    void updateRestaurant_modifieNomEtAdresse() {
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .id(1).name("Ancien nom").address("Ancienne adresse").build();
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(restaurant)).thenReturn(restaurant);

        RestaurantResponseDto resultat = restaurantService.updateRestaurant(1,
            new RestaurantUpdateDto("Nouveau nom", "Nouvelle adresse"));

        assertEquals("Nouveau nom", resultat.getName());
        assertEquals("Nouvelle adresse", resultat.getAddress());
    }

    @Test
    void updateRestaurant_inexistant_leveNotFoundEtNeSauvegardeRien() {
        when(restaurantRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> restaurantService.updateRestaurant(999,
                new RestaurantUpdateDto("X", "Y")));
        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void restaurantSansImage_imageUrlNull() {
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .id(1).name("Le Bistrot").address("Nancy").build();
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));

        assertNull(restaurantService.getRestaurantById(1).getPicture());
        verify(s3Repository, never()).getFileUrl(any(), any()); // S3 n'est pas appelé
    }

    @Test
    void restaurantAvecImage_imageUrlGenereeParS3() {
        RestaurantEntity restaurant = RestaurantEntity.builder()
                .id(1).name("Le Bistrot").address("Nancy").picture("Afiwa/restaurant1.jpg").build();
        when(restaurantRepository.findById(1)).thenReturn(Optional.of(restaurant));
        when(s3Repository.getFileUrl("restaurant", "Afiwa/restaurant1.jpg"))
                .thenReturn("https://s3/lien-de-lecture");

        assertEquals("https://s3/lien-de-lecture", restaurantService.getRestaurantById(1).getPicture());
    }
}