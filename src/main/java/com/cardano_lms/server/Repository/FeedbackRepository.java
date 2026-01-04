package com.cardano_lms.server.Repository;

import com.cardano_lms.server.constant.FeedbackStatus;
import com.cardano_lms.server.Entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.replies WHERE f.id = :id")
    Optional<Feedback> findByIdWithReplies(@Param("id") Long id);
    
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.course WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Feedback> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    List<Feedback> findByCourseIdAndStatus(String courseId, FeedbackStatus status);

    List<Feedback> findByCourseId(String courseId);

    boolean existsByCourseIdAndUserId(String courseId, String userId);
    
    boolean existsByCourseIdAndUserIdAndParentIsNull(String courseId, String userId);

    Page<Feedback> findByCourseId(String courseId, Pageable pageable);

    Page<Feedback> findByCourseIdAndStatus(String courseId, FeedbackStatus status, Pageable pageable);

    List<Feedback> findByCourseIdAndStatusIn(String courseId, List<FeedbackStatus> statuses);

    Page<Feedback> findByCourseIdAndStatusIn(String courseId, List<FeedbackStatus> statuses, Pageable pageable);

    List<Feedback> findByCourseIdAndParentIsNullAndStatusOrderByCreatedAtDesc(String courseId, FeedbackStatus status);

    List<Feedback> findByCourseIdAndParentIsNullAndStatusInOrderByCreatedAtDesc(String courseId, List<FeedbackStatus> statuses);

    Page<Feedback> findByCourseIdAndParentIsNullAndStatusOrderByCreatedAtDesc(String courseId, FeedbackStatus status, Pageable pageable);

    Page<Feedback> findByCourseIdAndParentIsNullAndStatusInOrderByCreatedAtDesc(String courseId, List<FeedbackStatus> statuses, Pageable pageable);

    interface CourseRatingAgg {
        String getCourseId();

        Double getAvgRating();

        Long getReviewCount();
    }

    @Query("SELECT f.course.id AS courseId, AVG(f.rate) AS avgRating, COUNT(f.id) AS reviewCount " +
            "FROM Feedback f " +
            "WHERE f.status = :status AND f.course.id IN :courseIds AND f.parent IS NULL " +
            "GROUP BY f.course.id")
    List<CourseRatingAgg> aggregateRatingsByCourseIds(@Param("courseIds") List<String> courseIds,
            @Param("status") FeedbackStatus status);
    
    
    @Query("SELECT f FROM Feedback f " +
           "LEFT JOIN FETCH f.course c " +
           "WHERE c.instructor.id = :instructorId " +
           "ORDER BY f.createdAt DESC")
    List<Feedback> findByInstructorCoursesOrderByCreatedAtDesc(@Param("instructorId") String instructorId);
    
    
    @Query("SELECT COUNT(f) FROM Feedback f " +
           "WHERE f.course.instructor.id = :instructorId " +
           "AND f.user.id != :instructorId")
    long countByInstructorCoursesNotOwn(@Param("instructorId") String instructorId);
    
    
    @Query("SELECT f FROM Feedback f " +
           "LEFT JOIN FETCH f.course " +
           "WHERE f.parent.user.id = :userId " +
           "AND f.user.id != :userId " +
           "ORDER BY f.createdAt DESC")
    List<Feedback> findRepliesToUserFeedbacks(@Param("userId") String userId);
}
