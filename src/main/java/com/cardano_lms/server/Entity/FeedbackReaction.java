package com.cardano_lms.server.Entity;

import com.cardano_lms.server.constant.ReactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "feedback_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    private LocalDateTime createdAt;
}







