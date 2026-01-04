package com.cardano_lms.server.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lecture_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    
    @Column(name = "lecture_title_snapshot")
    private String lectureTitleSnapshot;
    
    
    @Column(name = "course_id")
    private String courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private LectureComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LectureComment> children = new ArrayList<>();

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "visible", nullable = false)
    @Builder.Default
    private boolean visible = true;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private int likeCount = 0;
}
