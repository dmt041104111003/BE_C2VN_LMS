package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Nonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NonceRepository extends JpaRepository<Nonce, Long> {
    Boolean existsByNonce(String nonce);
    Optional<Nonce> findByNonce(String nonce);
    void deleteByNonce(String nonce);
}
