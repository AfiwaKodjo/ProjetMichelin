package projetMichelin.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class RestaurantResponseDto {

    private Integer id;
    private String name;
    private String address;
    //private String picture;
    private double moyenne;

    

}
