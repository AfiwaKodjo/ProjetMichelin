package projetMichelin.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import projetMichelin.dto.restaurant.RestaurantCreateRequestDto;
import projetMichelin.dto.restaurant.RestaurantResponseDto;
import projetMichelin.dto.restaurant.RestaurantUpdateDto;
import projetMichelin.entity.EvaluationEntity;
import projetMichelin.entity.RestaurantEntity;
import projetMichelin.exceptions.NotFoundException;
import projetMichelin.repository.RestaurantRepository;
import projetMichelin.repository.S3Repository;

@Service 
@RequiredArgsConstructor
@Transactional 
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final S3Repository s3Repository;
    private static final String BUCKET="restaurant";

    //Récupérer tous les restaurants avec leur moyenne
    public List<RestaurantResponseDto> getRestaurants(){
        List<RestaurantResponseDto> resultat = new ArrayList<>();
        for(RestaurantEntity restaurant: restaurantRepository.findAll()){
            resultat.add(toDto(restaurant));
        }
        return resultat;
    }

    //On cherche le restaurant dans la base et on le renvoie. S'il est vide, on lève une exception, pour éviter de refaire à chaque fois
    public RestaurantEntity findRestaurant(Integer id){
        return restaurantRepository.findById(id)
        .orElseThrow(()->new NotFoundException("Le restaurant avec id "+id+" n'existe pas"));

    }

    //Récupérer un seul restaurant
    public RestaurantResponseDto getRestaurantById(Integer id){
        return toDto(findRestaurant(id));
    }

    //Créer un restaurant
    public RestaurantResponseDto createRestaurant(RestaurantCreateRequestDto dto){
        RestaurantEntity restaurant = RestaurantEntity.builder().name(dto.getName())
        .address(dto.getAddress())
        .build();

     return toDto(restaurantRepository.save(restaurant));
    }

    //mettre à jour le nom et l'adresse d'un restaurant
    public RestaurantResponseDto updateRestaurant(Integer id, RestaurantUpdateDto dto){
        RestaurantEntity restaurant = findRestaurant(id);
        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        return toDto(restaurantRepository.save(restaurant));

    }

    //On calcule la moyenne des notes du restaurant et on renvoie -1 quand y en a pas
    private RestaurantResponseDto toDto(RestaurantEntity restaurant){
        double moyenne = restaurant.getEvaluations().stream().mapToInt(EvaluationEntity::getNote)
                  .average().orElse(-1);
        //lien pour voir l'image
        String pictureUrl = null;
        if (restaurant.getPicture() != null) {
            pictureUrl = s3Repository.getFileUrl(BUCKET, restaurant.getPicture());
        }
        return new RestaurantResponseDto(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getAddress(),
            moyenne,
            pictureUrl
        );

    } 

    public String getPictureUrl(Integer id){
        RestaurantEntity restaurantEntity = findRestaurant(id);
        String key = "Afiwa/restaurants"+id+UUID.randomUUID()+".jpg";
        restaurantEntity.setPicture(key);
        restaurantRepository.save(restaurantEntity);
        return s3Repository.getUploadFileUrl(BUCKET,key);

    }
}







