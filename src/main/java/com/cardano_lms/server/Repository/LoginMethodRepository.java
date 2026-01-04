package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.LoginMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginMethodRepository extends JpaRepository<LoginMethod, String> {
    Boolean existsByName(String loginMethod);
}
