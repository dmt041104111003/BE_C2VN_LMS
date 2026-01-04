package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Certificate;
import com.cardano_lms.server.constant.CertificateNFTStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    @Query("SELECT c FROM Certificate c WHERE c.enrollment.user.id = :userId")
    List<Certificate> findAllByUserId(@Param("userId") Long userId);

    List<Certificate> findByEnrollmentId(Long enrollmentId);

    List<Certificate> findByEnrollment_User_Id(String enrollment_user_id);

    boolean existsByEnrollmentId(Long enrollmentId);

    @Query("SELECT c FROM Certificate c WHERE c.certificateNFTStatus = :status " +
           "AND c.enrollment.walletAddress IS NOT NULL " +
           "AND c.enrollment.walletAddress <> '' " +
           "ORDER BY c.enrollment.course.id")
    List<Certificate> findByStatusWithWallet(@Param("status") CertificateNFTStatus status);

    java.util.Optional<Certificate> findByPolicyIdAndAssetName(String policyId, String assetName);

    @Query("SELECT c FROM Certificate c WHERE c.enrollment.walletAddress = :walletAddress AND c.enrollment.course.title = :courseTitle")
    java.util.Optional<Certificate> findByWalletAddressAndCourseTitle(@Param("walletAddress") String walletAddress, @Param("courseTitle") String courseTitle);
}
