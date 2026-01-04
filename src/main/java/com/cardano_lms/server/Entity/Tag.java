package com.cardano_lms.server.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.slug = generateSlug(this.name);
    }

    private String generateSlug(String input) {
        if (input == null) return null;
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    @ManyToMany(mappedBy = "courseTags")
    @JsonBackReference
    private List<Course> courses = new ArrayList<>();

}
