package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String>, JpaSpecificationExecutor<Course> {
    Page<Course> findAllByInstructorId(String instructorId, Pageable pageable);
    Page<Course> findAllByInstructorIdAndDraftFalse(String instructorId, Pageable pageable);
    Page<Course> findAllByInstructorIdAndDraftTrue(String instructorId, Pageable pageable);
    List<Course> findAllByInstructorIdAndDraftFalse(String instructorId);
    List<Course> findAllByInstructorId(String instructorId);
    List<Course> findAllByDraftFalse();
    
    Optional<Course> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByTitleIgnoreCase(String title);
}
