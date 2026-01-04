package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;
    private int score;
    private int orderIndex;
    private String imageUrl;
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne @JoinColumn(name = "test_id")
    @JsonIgnore
    @ToString.Exclude
    private Test test;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Answer> answers = new ArrayList<>();

    public void addAnswer(Answer answer) {
        answers.add(answer);
        answer.setQuestion(this);
    }
}
