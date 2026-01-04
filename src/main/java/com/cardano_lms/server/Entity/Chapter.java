package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapters")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Setter @Getter
public class Chapter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int orderIndex;

    @ManyToOne @JoinColumn(name = "course_id")
    @JsonIgnore
    @ToString.Exclude
    private Course course;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Lecture> lectures = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Test> tests = new ArrayList<>();

    public void addLecture(Lecture lecture) {
        lectures.add(lecture);
        lecture.setChapter(this);
    }

    public void addTest(Test test) {
        tests.add(test);
        test.setChapter(this);
    }
}
