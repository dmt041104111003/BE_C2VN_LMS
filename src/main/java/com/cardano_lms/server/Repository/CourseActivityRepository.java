package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.CourseActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseActivityRepository extends JpaRepository<CourseActivity, Long> {
    Page<CourseActivity> findByCourseIdOrderByTimestampDesc(String courseId, Pageable pageable);
    
    List<CourseActivity> findByCourseIdAndTimestampAfterOrderByTimestampDesc(
            String courseId, LocalDateTime after);
    
    List<CourseActivity> findByCourseIdOrderByTimestampDesc(String courseId);
    
    void deleteByCourseId(String courseId);
}


