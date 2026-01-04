package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.VerificationToken;
import com.cardano_lms.server.constant.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findTopByEmailAndTypeAndUsedOrderByCreatedAtDesc(String email, TokenType type, boolean used);
    
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < ?1")
    void deleteExpired(Instant now);
    
    @Modifying
    void deleteByEmailAndType(String email, TokenType type);
}

