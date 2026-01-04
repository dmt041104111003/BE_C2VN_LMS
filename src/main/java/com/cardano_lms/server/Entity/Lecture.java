package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lectures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String videoUrl;
    private int time;
    private int orderIndex;
    private String resourceUrl;
    private String resourceType;
    private Boolean previewFree;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    @JsonIgnore
    @ToString.Exclude
    private Chapter chapter;
}
