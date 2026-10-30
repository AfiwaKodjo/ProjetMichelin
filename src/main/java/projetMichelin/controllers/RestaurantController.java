package projetMichelin.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import projetMichelin.dto.restaurant.RestaurantResponseDto;
import org.springframework.web.bind.annotation.RequestParam;



@RestController 
public class RestaurantController {

    private final List<RestaurantResponseDto> restaurants = new ArrayList<>();

    //Récupérer tous les restaurants
    @GetMapping("/restaurants")
    public List<RestaurantResponseDto> getAll(){
        return restaurants;
    }

    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantResponseDto> getById(@PathVariable Integer id) {
        return restaurants.stream()
        .filter(r->r.getId().equals(id))
        .findFirst()
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("path")
    public String postMethodName(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
        

    


}
