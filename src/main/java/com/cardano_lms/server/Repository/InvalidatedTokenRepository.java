package com.cardano_lms.server.Repository;


import com.cardano_lms.server.Entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {}
