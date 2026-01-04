package com.cardano_lms.server.Entity;

import com.cardano_lms.server.constant.TokenType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "verification_tokens", indexes = {
    @Index(name = "idx_token_email", columnList = "email"),
    @Index(name = "idx_token_code", columnList = "code"),
    @Index(name = "idx_token_expires", columnList = "expiresAt")
})
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TokenType type;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    String code;

    @Column(nullable = false)
    Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    boolean used = false;

    @Column(nullable = false)
    @Builder.Default
    Instant createdAt = Instant.now();

    @Column(nullable = false)
    @Builder.Default
    int attempts = 0;
}

