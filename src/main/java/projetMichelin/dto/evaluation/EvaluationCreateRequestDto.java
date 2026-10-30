package projetMichelin.dto.evaluation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class EvaluationCreateRequestDto {


    @NotBlank 
    @Size(max=255)
    private String comment;

    @NotNull 
    @Min(0)
    @Max(3)
    private Integer note;

}
