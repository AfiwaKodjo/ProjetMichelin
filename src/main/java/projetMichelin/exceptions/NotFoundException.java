package projetMichelin.exceptions;

public class NotFoundException extends RuntimeException{
    
    //le message sera renvoyé au client dans {code,message}
    public NotFoundException(String message){
        super(message);
    }

}
