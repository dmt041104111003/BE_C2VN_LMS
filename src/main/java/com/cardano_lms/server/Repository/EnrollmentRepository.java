package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Enrollment;
import com.cardano_lms.server.Entity.User;
import com.cardano_lms.server.constant.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByOrderId(String orderId);
    Optional<Enrollment> findByUser_IdAndCourse_Id(String userId, String courseId);
    boolean existsByUserIdAndCourseId(String userId, String courseId);
    List<Enrollment> findAllByUser_Id(String userId);
    long countByCourse_Id(String courseId);
    
    boolean existsByCourse_IdAndStatusIn(String courseId, Collection<OrderStatus> statuses);

    String user(User user);

    @Query("SELECT e FROM Enrollment e " +
           "JOIN e.progress p " +
           "WHERE p.courseCompleted = true " +
           "AND (p.certificateIssued = false OR p.certificateIssued IS NULL) " +
           "AND e.walletAddress IS NOT NULL " +
           "AND e.walletAddress <> '' " +
           "AND e.course.price IS NOT NULL " +
           "AND e.course.price > 0 " +
           "ORDER BY e.course.id")
    List<Enrollment> findEligibleForCertificate();
}