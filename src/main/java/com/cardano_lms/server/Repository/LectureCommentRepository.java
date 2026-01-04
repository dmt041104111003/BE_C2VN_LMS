package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.LectureComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureCommentRepository extends JpaRepository<LectureComment, Long> {
    List<LectureComment> findByLecture_IdAndParentIsNullOrderByCreatedAtDesc(Long lectureId);
    List<LectureComment> findByLecture_IdOrderByCreatedAtDesc(Long lectureId);
    
    @Query("SELECT c FROM LectureComment c " +
           "JOIN FETCH c.lecture l " +
           "JOIN FETCH l.chapter ch " +
           "JOIN FETCH ch.course " +
           "WHERE c.user.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<LectureComment> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    
    
    @Query("SELECT c FROM LectureComment c " +
           "JOIN FETCH c.lecture l " +
           "JOIN FETCH l.chapter ch " +
           "JOIN FETCH ch.course course " +
           "WHERE course.instructor.id = :instructorId " +
           "ORDER BY c.createdAt DESC")
    List<LectureComment> findByInstructorCoursesOrderByCreatedAtDesc(@Param("instructorId") String instructorId);
    
    
    @Query("SELECT COUNT(c) FROM LectureComment c " +
           "JOIN c.lecture l " +
           "JOIN l.chapter ch " +
           "JOIN ch.course course " +
           "WHERE course.instructor.id = :instructorId " +
           "AND c.user.id != :instructorId")
    long countByInstructorCoursesNotOwn(@Param("instructorId") String instructorId);
    
    
    @Query("SELECT c FROM LectureComment c " +
           "JOIN FETCH c.lecture l " +
           "JOIN FETCH c.user " +
           "JOIN l.chapter ch " +
           "WHERE ch.course.id = :courseId " +
           "ORDER BY c.createdAt DESC")
    List<LectureComment> findByCourseIdOrderByCreatedAtDesc(@Param("courseId") String courseId);
    
    
    @Query("SELECT c FROM LectureComment c LEFT JOIN FETCH c.user WHERE c.courseId = :courseId ORDER BY c.createdAt DESC")
    List<LectureComment> findAllByCourseIdIncludingDeleted(@Param("courseId") String courseId);
    
    
    @Query("SELECT c FROM LectureComment c " +
           "JOIN FETCH c.lecture l " +
           "JOIN FETCH l.chapter ch " +
           "JOIN FETCH ch.course " +
           "WHERE c.parent.user.id = :userId " +
           "AND c.user.id != :userId " +
           "ORDER BY c.createdAt DESC")
    List<LectureComment> findRepliesToUserComments(@Param("userId") String userId);
    
    
    @Modifying
    @Query("DELETE FROM LectureComment c WHERE c.lecture.id = :lectureId")
    void deleteByLectureId(@Param("lectureId") Long lectureId);
    
    
    @Query("SELECT c.id FROM LectureComment c WHERE c.lecture.id = :lectureId")
    List<Long> findIdsByLectureId(@Param("lectureId") Long lectureId);
}
