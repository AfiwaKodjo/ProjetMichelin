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
import projetMichelin.dto.evaluation.EvaluationUpdateDto;
import projetMichelin.entity.EvaluationEntity;
import projetMichelin.entity.RestaurantEntity;
import projetMichelin.exceptions.NotFoundException;
import projetMichelin.repository.EvaluationRepository;
import projetMichelin.repository.S3Repository;
import projetMichelin.services.indexation.IndexService;

@Service 
@RequiredArgsConstructor
@Transactional
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final RestaurantService restaurantService;
    private final S3Repository s3Repository;
    private static final String BUCKET="evaluation";
    private final IndexService indexService;


    //ajouter une évaluation sur un restaurant
    public EvaluationResponseDto createEvaluation(Integer restaurantId, EvaluationCreateRequestDto dto, String reviewerName){
        RestaurantEntity restaurant = restaurantService.findRestaurant(restaurantId);
        EvaluationEntity evaluation = EvaluationEntity.builder().reviewerName(reviewerName).comment(dto.getComment())
                                                      .note(dto.getNote()).restaurant(restaurant).build();

        EvaluationEntity saved = evaluationRepository.save(evaluation);
        indexService.indexComment(saved.getId(), saved.getComment());
        return toDto(saved);
    }

    //Supprimer une évaluation
    public void deleteEvaluation(Integer restaurantId, Integer id, String userName, boolean isAdmin ){
        EvaluationEntity evaluation = evaluationRepository.findById(id)
                                      .orElseThrow(() -> new NotFoundException("L'évaluation " + id + " n'existe pas"));
        if (!(evaluation.getRestaurant().getId()).equals(restaurantId)){
            throw new NotFoundException("cette évaluation " + id
                + " n'appartient pas au restaurant " + restaurantId);
        }
    boolean estAuteur = evaluation.getReviewerName().equals(userName);
    if (!estAuteur && !isAdmin) {
        throw new AccessDeniedException("Ce n'est pas votre évaluation"); 
    }

        indexService.deleteComment(evaluation.getId());
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
        String key = "Afiwa/evaluation"+evaluationId+"-"+UUID.randomUUID() +".jpg";
        evaluation.getPictures().add(key);
        evaluationRepository.save(evaluation);
        return s3Repository.getUploadFileUrl(BUCKET, key);
    }

        //Recherche par des mots-clés
    public List<EvaluationResponseDto> searchEvaluations(String motsCles) {
        if (motsCles == null || motsCles.isBlank()) {
            return new ArrayList<>();}

        List<Integer> ids = indexService.searchComment(motsCles);

        List<EvaluationResponseDto> resultat = new ArrayList<>();
        for (EvaluationEntity evaluation: evaluationRepository.findAllById(ids)) {
            resultat.add(toDto(evaluation));
        }
        return resultat;
    }
    
    //Modifier une évaluation
    public EvaluationResponseDto updateEvaluation(Integer restaurantId, Integer id, EvaluationUpdateDto dto,String userName, boolean isAdmin) {
        EvaluationEntity evaluation = findEvaluation(id);

        if (!evaluation.getRestaurant().getId().equals(restaurantId)) {
            throw new NotFoundException("L'évaluation " + id + " n'appartient pas au restaurant " + restaurantId);
        }
        boolean estAuteur = evaluation.getReviewerName().equals(userName);
        if (!estAuteur && !isAdmin) {
            throw new AccessDeniedException("Ce n'est pas votre évaluation");
        }

        evaluation.setComment(dto.getComment());
        evaluation.setNote(dto.getNote());
        EvaluationEntity saved = evaluationRepository.save(evaluation);

        //on remplace l'ancien commentaire dans l'index
        indexService.deleteComment(saved.getId());
        indexService.indexComment(saved.getId(), saved.getComment());
        return toDto(saved);
    }
}










