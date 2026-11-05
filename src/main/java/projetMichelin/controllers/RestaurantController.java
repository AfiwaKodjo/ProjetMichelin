package projetMichelin.controllers;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import projetMichelin.dto.UrlResponseDto;
import projetMichelin.dto.restaurant.RestaurantCreateRequestDto;
import projetMichelin.dto.restaurant.RestaurantResponseDto;
import projetMichelin.dto.restaurant.RestaurantUpdateDto;
import projetMichelin.services.RestaurantService;

@Tag(name = "Restaurants")
@RestController
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    //Pour tous les restaurants
    @Operation(summary = "Lister tous les restaurants")
    @GetMapping("/restaurants")
    public List<RestaurantResponseDto> getRestaurants(){
        return restaurantService.getRestaurants();
    }

    //Pour un restaurant en particulier
    @Operation(summary = "Récupérer un restaurant")
    @GetMapping("/restaurants/{id}")
    public RestaurantResponseDto getById(@PathVariable Integer id) {
        return restaurantService.getRestaurantById(id);
    }

    //Pour créer un restaurant
    @Operation(summary = "Créer un restaurant")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/restaurants")
    public ResponseEntity<RestaurantResponseDto> createRestaurant(@Valid @RequestBody RestaurantCreateRequestDto dto) {
        RestaurantResponseDto created = restaurantService.createRestaurant(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    //Pour modifier le nom et l'adresse
    @Operation(summary = "Modifier le nom et l'adresse d'un restaurant")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restaurants/{id}")
    public RestaurantResponseDto updateRestaurant(@PathVariable Integer id, @Valid @RequestBody RestaurantUpdateDto dto){
        return restaurantService.updateRestaurant(id,dto);
    }

    //Lien S3 pour envoyer l'image du restaurant
    @Operation(summary = "Obtenir un lien pour envoyer l'image du restaurant")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/restaurants/{id}/image")
    public UrlResponseDto getPictureUrl(@PathVariable Integer id) {
        return new UrlResponseDto(restaurantService.getPictureUrl(id));
    }
}
