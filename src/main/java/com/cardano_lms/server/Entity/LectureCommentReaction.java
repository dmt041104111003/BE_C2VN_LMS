package com.cardano_lms.server.Entity;

import com.cardano_lms.server.constant.ReactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_comment_reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "comment_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureCommentReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LectureComment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    private LocalDateTime createdAt;
}



