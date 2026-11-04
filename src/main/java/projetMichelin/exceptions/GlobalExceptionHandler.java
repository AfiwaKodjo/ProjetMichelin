package projetMichelin.exceptions;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j      
public class GlobalExceptionHandler {

    //on gère l'erreur 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
         //le message écrit dans le service
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, e.getMessage()));
    }


    //Pour l'erreur 400 quand les données ne sont pas valides
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        FieldError erreur = e.getBindingResult().getFieldErrors().get(0);
        String message = erreur.getField()+" : "+ erreur.getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
    }

    //mauvais type dans l'URL
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleBadType(MethodArgumentTypeMismatchException e) {
        String message = "Le paramètre '" + e.getName() + "' n'a pas le bon type";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
    }


    //Pour erreur 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception e) {
        log.error("Une erreur inattendue s'est produite", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, "Une erreur du serveur est survenue"));
    }

    //JSON mal écrit dans le body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, "Le corps de votre requête n'est pas valide"));
}

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccesNotAutorised(AccessDeniedException e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null||auth instanceof AnonymousAuthenticationToken) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(401, "Vous devez être connecté d'abord"));
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403, "Vous n'avez pas ses droits"));
}

    //lorsque la route n'existe pas
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRouteInconnue(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, "Cette route n'existe pas"));
    }

    //lorsqu'on a un paramètre manquant
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametreManquant(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, "Le paramètre" + e.getParameterName() + "n'est pas facultatif"));
    }

    //mauvaise méthode HTTP sur une route qui existe
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMauvaiseMethode(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(405, "La méthode " + e.getMethod() + " n'est pas autorisée sur cette route"));
    }
}
