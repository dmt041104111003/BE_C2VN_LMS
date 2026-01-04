package com.cardano_lms.server.Repository;


import com.cardano_lms.server.Entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsBySlug(String slug);

}
