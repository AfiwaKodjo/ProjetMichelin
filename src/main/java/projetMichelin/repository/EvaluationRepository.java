package projetMichelin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projetMichelin.entity.EvaluationEntity;
import java.util.List;


@Repository 
public interface EvaluationRepository extends JpaRepository<EvaluationEntity,Integer>{
     List<EvaluationEntity> findByReviewerName(String reviewerName);

    
} 