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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import projetMichelin.dto.UrlResponseDto;
import projetMichelin.dto.restaurant.RestaurantCreateRequestDto;
import projetMichelin.dto.restaurant.RestaurantResponseDto;
import projetMichelin.dto.restaurant.RestaurantUpdateDto;
import projetMichelin.services.RestaurantService;


@RestController 
@RequiredArgsConstructor 
public class RestaurantController {

    private final RestaurantService restaurantService;

    //Pour tous les restaurants
    @GetMapping("/restaurants")
    public List<RestaurantResponseDto> getRestaurants(){
        return restaurantService.getRestaurants();
    }

    //Pour un restaurant en particulier
    @GetMapping("/restaurants/{id}")
    public RestaurantResponseDto getById(@PathVariable Integer id) {
        return restaurantService.getRestaurantById(id);
    }

    //Pour créer un restauarant
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/restaurants")
    public ResponseEntity<RestaurantResponseDto> createRestaurant(@Valid @RequestBody RestaurantCreateRequestDto dto) {
        RestaurantResponseDto created = restaurantService.createRestaurant(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    //Pour modifier nom et l'adresse
     @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/restaurants/{id}")
    public RestaurantResponseDto updateRestaurant(@PathVariable Integer id, @Valid @RequestBody RestaurantUpdateDto dto){
        return restaurantService.updateRestaurant(id,dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/restaurants/{id}/image")
    public UrlResponseDto getPictureUrl(@PathVariable Integer id) {
        return new UrlResponseDto(restaurantService.getPictureUrl(id));
    }
    
    
        

    


}
