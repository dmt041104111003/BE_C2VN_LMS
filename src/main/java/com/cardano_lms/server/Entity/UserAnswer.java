package com.cardano_lms.server.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Builder
@Table(name = "user_answers")
@Data @NoArgsConstructor @AllArgsConstructor
public class UserAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "test_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Test test;

    
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "question_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Question question;

    
    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "answer_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Answer answer;

    
    @Column(name = "test_title_snapshot")
    private String testTitleSnapshot;

    @Column(name = "question_content_snapshot", columnDefinition = "TEXT")
    private String questionContentSnapshot;

    @Column(name = "answer_content_snapshot", columnDefinition = "TEXT")
    private String answerContentSnapshot;

    @Column(name = "is_correct_snapshot")
    private Boolean isCorrectSnapshot;
}
