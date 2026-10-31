package projetMichelin.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import projetMichelin.dto.evaluation.EvaluationCreateRequestDto;
import projetMichelin.dto.evaluation.EvaluationResponseDto;
import projetMichelin.services.EvaluationService;

@RestController 
@RequiredArgsConstructor 
public class EvaluationController {

   private final EvaluationService evaluationService;

    private static final String AUTEUR_TEMPORAIRE = "test";

    //On ajoute une évaluation à un restaurant
    @PostMapping("/restaurants/{restaurantId}/evaluations")
    public ResponseEntity<EvaluationResponseDto> createEvaluation(@PathVariable Integer restaurantId, @Valid @RequestBody EvaluationCreateRequestDto dto) {
        EvaluationResponseDto created =
                evaluationService.createEvaluation(restaurantId, dto, AUTEUR_TEMPORAIRE);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    //Supprimer une évaluation
    @DeleteMapping("/restaurants/{restaurantId}/evaluations/{evaluationId}")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Integer restaurantId, @PathVariable Integer evaluationId) {
        evaluationService.deleteEvaluation(restaurantId, evaluationId);
        return ResponseEntity.noContent().build();
    }

    // Les évaluations de l'utilisateur connecté
    @GetMapping("/evaluations/moi")
    public List<EvaluationResponseDto> getMyEvaluations() {
        return evaluationService.getEvaluations(AUTEUR_TEMPORAIRE);
    }


}
