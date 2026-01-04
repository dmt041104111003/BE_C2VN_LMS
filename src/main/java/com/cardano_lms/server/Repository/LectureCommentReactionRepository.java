package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.LectureCommentReaction;
import com.cardano_lms.server.constant.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureCommentReactionRepository extends JpaRepository<LectureCommentReaction, Long> {
    
    Optional<LectureCommentReaction> findByUserIdAndCommentId(String userId, Long commentId);
    
    long countByCommentIdAndType(Long commentId, ReactionType type);
    
    void deleteByUserIdAndCommentId(String userId, Long commentId);
    
    @Modifying
    @Query("DELETE FROM LectureCommentReaction r WHERE r.comment.id IN :commentIds")
    void deleteByCommentIdIn(@Param("commentIds") List<Long> commentIds);
}




