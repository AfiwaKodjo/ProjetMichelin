package projetMichelin.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter 
@AllArgsConstructor 
public class ErrorResponse {
    //code et message pour pouvoir expliquer les erreurs
    private int code;
    private String message;

}
