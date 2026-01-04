package com.cardano_lms.server.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrolled_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrolledSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", unique = true, nullable = false)
    @ToString.Exclude
    private Enrollment enrollment;

    
    private String originalCourseId;

    
    @Column(nullable = false)
    private String courseTitle;

    @Column(columnDefinition = "TEXT")
    private String courseDescription;

    @Column(columnDefinition = "TEXT")
    private String courseImageUrl;

    @Column(columnDefinition = "TEXT")
    private String courseVideoUrl;

    private String instructorName;
    private String instructorId;

    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String structureJson;

    
    private LocalDateTime courseVersionAt;
    
    
    @Builder.Default
    private Integer version = 1;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    
    private LocalDateTime upgradedAt;
}


