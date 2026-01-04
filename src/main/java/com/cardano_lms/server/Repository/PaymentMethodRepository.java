package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    boolean existsByName(String name);
    Optional<PaymentMethod> findByName(String name);
    List<PaymentMethod> findAllByNameIn(List<String> names);
}
