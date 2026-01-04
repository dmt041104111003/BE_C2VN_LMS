package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.LoginMethod;
import com.cardano_lms.server.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);
    boolean existsByGoogle(String google);
    boolean existsByWalletAddress(String walletAddress);
    boolean existsByGithub(String github);
    Optional<User> findByGoogle(String google);
    Optional<User> findByGithub(String github);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmailAndLoginMethod(String email, LoginMethod loginMethod);
    Optional<User> findByEmailAndLoginMethodName(String email, String loginMethodName);
    Optional<User> findByWalletAddress(String walletAddress);
}
