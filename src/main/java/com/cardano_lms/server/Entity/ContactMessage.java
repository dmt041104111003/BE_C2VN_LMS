package com.cardano_lms.server.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isRead = false;
    }
}
