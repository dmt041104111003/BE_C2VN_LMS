package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    
    @Query("SELECT t.id FROM Test t WHERE t.course.id = :courseId")
    List<Long> findTestIdsByCourseId(@Param("courseId") String courseId);
    
    @Query("SELECT t.id FROM Test t WHERE t.chapter.course.id = :courseId")
    List<Long> findTestIdsByChapterCourseId(@Param("courseId") String courseId);
    
    List<Test> findByCourse_IdOrderByOrderIndexAsc(String courseId);
    List<Test> findByChapter_IdOrderByOrderIndexAsc(Long chapterId);
}
