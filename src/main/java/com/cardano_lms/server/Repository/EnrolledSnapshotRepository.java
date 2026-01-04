package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.EnrolledSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EnrolledSnapshotRepository extends JpaRepository<EnrolledSnapshot, Long> {
    
    Optional<EnrolledSnapshot> findByEnrollmentId(Long enrollmentId);
    
    boolean existsByEnrollmentId(Long enrollmentId);
    
    @Query("SELECT s FROM EnrolledSnapshot s WHERE s.enrollment.user.id = :userId AND s.originalCourseId = :courseId")
    Optional<EnrolledSnapshot> findByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);
    
    
    @Modifying
    @Query("DELETE FROM EnrolledSnapshot s WHERE s.enrollment.completed = true " +
           "AND s.createdAt < :cutoff " +
           "AND s.enrollment.certificate IS NULL")
    int deleteCompletedWithoutCertificateOlderThan(@Param("cutoff") LocalDateTime cutoff);
    
    
    @Query("SELECT COUNT(s) FROM EnrolledSnapshot s WHERE s.enrollment.user.id = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Modifying
    void deleteByEnrollmentId(Long enrollmentId);
}




