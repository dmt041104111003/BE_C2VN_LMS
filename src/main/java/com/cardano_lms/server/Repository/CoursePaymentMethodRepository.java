package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.CoursePaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursePaymentMethodRepository extends JpaRepository<CoursePaymentMethod, Long> {}