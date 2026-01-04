package com.cardano_lms.server.Repository;

import com.cardano_lms.server.Entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    List<ContactMessage> findByIsReadFalseOrderByCreatedAtDesc();
    long countByIsReadFalse();
}
