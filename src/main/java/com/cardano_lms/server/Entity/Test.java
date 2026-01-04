package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tests")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Test {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private String title;
    private int durationMinutes;
    private String rule;
    private int passScore;
    private int orderIndex;

    @ManyToOne @JoinColumn(name = "course_id", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    private Course course;

    @ManyToOne @JoinColumn(name = "chapter_id", nullable = true)
    @JsonIgnore
    @ToString.Exclude
    private Chapter chapter;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Question> questions = new ArrayList<>();

    public void addQuestion(Question question) {
        questions.add(question);
        question.setTest(this);
    }
}
