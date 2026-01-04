package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    
    
    @Query("SELECT ua FROM UserAnswer ua LEFT JOIN FETCH ua.question LEFT JOIN FETCH ua.answer WHERE ua.user.id = :userId AND ua.test.id = :testId")
    List<UserAnswer> findByUserIdAndTestId(@Param("userId") String userId, @Param("testId") Long testId);

    
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.user.id = :userId AND ua.testTitleSnapshot = :testTitle")
    List<UserAnswer> findByUserIdAndTestTitleSnapshot(@Param("userId") String userId, @Param("testTitle") String testTitle);
    
    @Query("SELECT DISTINCT ua FROM UserAnswer ua " +
           "JOIN FETCH ua.question " +
           "JOIN FETCH ua.answer " +
           "JOIN FETCH ua.test t " +
           "LEFT JOIN FETCH t.course " +
           "LEFT JOIN FETCH t.chapter ch " +
           "LEFT JOIN FETCH ch.course " +
           "WHERE ua.user.id = :userId")
    List<UserAnswer> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT DISTINCT ua FROM UserAnswer ua " +
           "JOIN FETCH ua.question " +
           "JOIN FETCH ua.answer " +
           "JOIN FETCH ua.test t " +
           "LEFT JOIN FETCH t.course tc " +
           "LEFT JOIN FETCH t.chapter ch " +
           "LEFT JOIN FETCH ch.course chc " +
           "WHERE ua.user.id = :userId AND (tc.id = :courseId OR chc.id = :courseId)")
    List<UserAnswer> findByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);
    
    void deleteByUserIdAndTestId(String userId, Long testId);

    
    @Modifying
    @Query("DELETE FROM UserAnswer ua WHERE ua.user.id = :userId AND " +
           "(ua.test.course.id = :courseId OR ua.test.chapter.course.id = :courseId)")
    void deleteByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);
}
