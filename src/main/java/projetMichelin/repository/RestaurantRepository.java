package projetMichelin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projetMichelin.entity.RestaurantEntity;

@Repository 
public interface RestaurantRepository extends JpaRepository<RestaurantEntity,Integer>{
   

    
} 