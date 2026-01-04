package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByChapter_IdOrderByOrderIndexAsc(Long chapterId);
}
