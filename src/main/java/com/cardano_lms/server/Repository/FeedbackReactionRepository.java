package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.FeedbackReaction;
import com.cardano_lms.server.constant.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackReactionRepository extends JpaRepository<FeedbackReaction, Long> {
    
    Optional<FeedbackReaction> findByUserIdAndFeedbackId(String userId, Long feedbackId);
    
    long countByFeedbackIdAndType(Long feedbackId, ReactionType type);
    
    void deleteByUserIdAndFeedbackId(String userId, Long feedbackId);
}







