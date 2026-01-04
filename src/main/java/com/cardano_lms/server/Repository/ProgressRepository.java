package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    
    Optional<Progress> findByEnrollment_Id(Long enrollmentId);
    
    void deleteByEnrollment_Id(Long enrollmentId);
}
