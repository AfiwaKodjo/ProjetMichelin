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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import projetMichelin.dto.UrlResponseDto;
import projetMichelin.dto.evaluation.EvaluationCreateRequestDto;
import projetMichelin.dto.evaluation.EvaluationResponseDto;
import projetMichelin.dto.evaluation.EvaluationUpdateDto;
import projetMichelin.services.EvaluationService;

@Tag(name = "Évaluations")
@RestController
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    //On ajoute une évaluation à un restaurant
    @Operation(summary = "Ajouter une évaluation à un restaurant")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/restaurants/{restaurantId}/evaluations")
    public ResponseEntity<EvaluationResponseDto> createEvaluation(@PathVariable Integer restaurantId, @Valid @RequestBody EvaluationCreateRequestDto dto,
         Authentication authentication) {
        EvaluationResponseDto created = evaluationService.createEvaluation(restaurantId, dto, userName(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    //Modifier une évaluation
    @Operation(summary = "Modifier une évaluation")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/restaurants/{restaurantId}/evaluations/{evaluationId}")
    public EvaluationResponseDto updateEvaluation(@PathVariable Integer restaurantId, @PathVariable Integer evaluationId,
        @Valid @RequestBody EvaluationUpdateDto dto, Authentication authentication) {
        return evaluationService.updateEvaluation(restaurantId, evaluationId, dto, userName(authentication), isAdmin(authentication));
    }

    //Supprimer une évaluation
    @Operation(summary = "Supprimer une évaluation")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/restaurants/{restaurantId}/evaluations/{evaluationId}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Integer restaurantId, @PathVariable Integer evaluationId,
        Authentication authentication) {
        evaluationService.deleteEvaluation(restaurantId, evaluationId, userName(authentication), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    //Ajouter une photo à son évaluation
    @Operation(summary = "Obtenir un lien pour envoyer une photo du plat")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/restaurants/{restaurantId}/evaluations/{evaluationId}/photos")
    public UrlResponseDto getPictureUrl(@PathVariable Integer restaurantId, @PathVariable Integer evaluationId, Authentication authentication) {
        String url = evaluationService.getPictureUrl(restaurantId, evaluationId, userName(authentication), isAdmin(authentication));
        return new UrlResponseDto(url);
    }

    //Les évaluations de l'utilisateur connecté
    @Operation(summary = "Récupérer mes propres évaluations")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/evaluations/moi")
    public List<EvaluationResponseDto> getMyEvaluations(Authentication authentication) {
        return evaluationService.getEvaluations(userName(authentication));
    }

    //recherche par mots clés
    @Operation(summary = "Rechercher des évaluations par des mots-clés")
    @GetMapping("/evaluations")
    public List<EvaluationResponseDto> searchEvaluations(@RequestParam String motsCles) {
        return evaluationService.searchEvaluations(motsCles);
    }

    //le nom de l'utilisateur dans le token
    private String userName(Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("name");
    }

    //si l'utilisateur a le rôle ADMIN
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
    }
}
