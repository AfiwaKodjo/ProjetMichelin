package projetMichelin.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import projetMichelin.dto.evaluation.EvaluationCreateRequestDto;
import projetMichelin.dto.evaluation.EvaluationResponseDto;
import projetMichelin.entity.EvaluationEntity;
import projetMichelin.entity.RestaurantEntity;
import projetMichelin.exceptions.NotFoundException;
import projetMichelin.repository.EvaluationRepository;
import projetMichelin.repository.S3Repository;

@Service 
@RequiredArgsConstructor
@Transactional
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final RestaurantService restaurantService;
    private final S3Repository s3Repository;
    private static final String BUCKET="evaluation";


    //ajouter une évaluation sur un restaurant
    public EvaluationResponseDto createEvaluation(Integer restaurantId, EvaluationCreateRequestDto dto, String reviewerName){
        RestaurantEntity restaurant = restaurantService.findRestaurant(restaurantId);
        EvaluationEntity evaluation = EvaluationEntity.builder().reviewerName(reviewerName).comment(dto.getComment())
                                                      .note(dto.getNote()).restaurant(restaurant).build();
          return toDto(evaluationRepository.save(evaluation));
    }

    //Supprimer une évaluation
    public void deleteEvaluation(Integer restaurantId, Integer id, String userName, boolean isAdmin ){
        EvaluationEntity evaluation = evaluationRepository.findById(id)
                                      .orElseThrow(() -> new NotFoundException("L'évaluation " + id + " n'existe pas"));
        if (!(evaluation.getRestaurant().getId()).equals(restaurantId)){
            throw new NotFoundException("cet évaluation " + id
                + " n'appartient pas au restaurant " + restaurantId);
        }
    boolean estAuteur = evaluation.getReviewerName().equals(userName);
    if (!estAuteur && !isAdmin) {
        throw new AccessDeniedException("Attention, Ce n'est pas votre évaluation"); 
    }

        evaluationRepository.delete(evaluation);
    }


    //Récupérer les évaluations que lui même a créé
    public List<EvaluationResponseDto> getEvaluations(String reviewerName){
        List<EvaluationResponseDto> resultat = new ArrayList<>();
        for (EvaluationEntity evaluation: evaluationRepository.findByReviewerName(reviewerName)){
                    resultat.add(toDto(evaluation));
                }
        return resultat;
        }
       

    // on récupère l'évaluation
    public EvaluationEntity findEvaluation(Integer id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("L'évaluation avec l'id " + id + " n'existe pas"));
    }

    // Dto de réponse
    private EvaluationResponseDto toDto(EvaluationEntity evaluation) {
        List<String> urls = new ArrayList<>();
        for(String key: evaluation.getPictures()){
            urls.add(s3Repository.getFileUrl(BUCKET, key));
        }
        return new EvaluationResponseDto(
                evaluation.getId(),evaluation.getReviewerName(),evaluation.getComment(),
                evaluation.getNote(),
                evaluation.getRestaurant().getId(),
                urls);
    }


    //on donne un lien pour envoyer une photo
    public String getPictureUrl(Integer restaurantId, Integer evaluationId, String utilisateur, boolean isAdmin) {
        EvaluationEntity evaluation = findEvaluation(evaluationId);

        if (!evaluation.getRestaurant().getId().equals(restaurantId)) {
            throw new NotFoundException("L'évaluation " + evaluationId+ " n'appartient pas au restaurant " + restaurantId);
        }

        boolean isAuthor = evaluation.getReviewerName().equals(utilisateur);
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Ce n'est pas votre évaluation");
        }
        String key = "Afiwa/evaluation" + evaluationId + "-" + UUID.randomUUID() + ".jpg";
        evaluation.getPictures().add(key);
        evaluationRepository.save(evaluation);
        return s3Repository.getUploadFileUrl(BUCKET, key);
    }
}










