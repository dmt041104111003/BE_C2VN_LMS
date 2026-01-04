package com.cardano_lms.server.Entity;
import com.cardano_lms.server.constant.FeedbackStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feedbacks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rate;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.VISIBLE;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    
    
    @Column(name = "course_title_snapshot")
    private String courseTitleSnapshot;
    
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Feedback parent;
    
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Feedback> replies = new ArrayList<>();
}
