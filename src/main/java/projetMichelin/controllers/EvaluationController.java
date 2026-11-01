package projetMichelin.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import projetMichelin.dto.UrlResponseDto;
import projetMichelin.dto.evaluation.EvaluationCreateRequestDto;
import projetMichelin.dto.evaluation.EvaluationResponseDto;
import projetMichelin.services.EvaluationService;

@RestController 
@RequiredArgsConstructor 
public class EvaluationController {

   private final EvaluationService evaluationService;

    //On ajoute une évaluation à un restaurant
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/restaurants/{restaurantId}/evaluations")
    public ResponseEntity<EvaluationResponseDto> createEvaluation(@PathVariable Integer restaurantId, @Valid @RequestBody EvaluationCreateRequestDto dto, 
         Authentication authentication) {
        EvaluationResponseDto created=evaluationService.createEvaluation(restaurantId, dto, userName(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    //Supprimer une évaluation
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/restaurants/{restaurantId}/evaluations/{evaluationId}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Integer restaurantId, @PathVariable Integer evaluationId,
        Authentication authentication) {
        evaluationService.deleteEvaluation(restaurantId, evaluationId, userName(authentication), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    //Les évaluations de l'utilisateur connecté
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/evaluations/moi")
    public List<EvaluationResponseDto> getMyEvaluations(Authentication authentication) {
        return evaluationService.getEvaluations(userName(authentication));
    }

    public String userName(Authentication authentication){
        Jwt jwt = (Jwt)authentication.getPrincipal();
        return jwt.getClaimAsString("name");
    }

      //si user a le rôle ADMIN
    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
    }

    //Ajouter une photo à son évaluation
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/restaurants/{restaurantId}/evaluations/{evaluationId}/photos")
    public UrlResponseDto getPictureUrl(@PathVariable Integer restaurantId,@PathVariable Integer evaluationId,Authentication authentication) {
        String url = evaluationService.getPictureUrl(restaurantId, evaluationId, userName(authentication), isAdmin(authentication));
        return new UrlResponseDto(url);
    }
}
