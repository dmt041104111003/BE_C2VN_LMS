package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.LectureComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureQnaRepository extends JpaRepository<LectureComment, Long> {
    List<LectureComment> findByLecture_IdOrderByCreatedAtDesc(Long lectureId);
}
