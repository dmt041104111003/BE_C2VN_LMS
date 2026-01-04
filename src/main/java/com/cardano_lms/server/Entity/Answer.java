package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private boolean correct;

    @ManyToOne @JoinColumn(name = "question_id")
    @JsonIgnore
    @ToString.Exclude
    private Question question;
}
