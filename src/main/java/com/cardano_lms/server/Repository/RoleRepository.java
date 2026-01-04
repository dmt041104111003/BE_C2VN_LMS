package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}
