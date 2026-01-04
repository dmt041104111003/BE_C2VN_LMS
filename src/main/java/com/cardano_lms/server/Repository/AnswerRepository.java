package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Answer;
import com.cardano_lms.server.Entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Repository
    interface LectureRepository  extends JpaRepository<Lecture, Long> {
    }
}