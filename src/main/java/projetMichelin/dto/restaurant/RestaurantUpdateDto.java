package projetMichelin.dto.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class RestaurantUpdateDto {
     @NotBlank 
    @Size(max=90)
    private String name;

    @NotBlank
    @Size(max=255)
    private String address;

}
