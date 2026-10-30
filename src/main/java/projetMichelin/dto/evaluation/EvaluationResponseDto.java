package projetMichelin.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class EvaluationResponseDto {

    private Integer id;
    private String reviewerName;
    private String comment;
    private Integer note;
    private Integer restaurantId;

}
