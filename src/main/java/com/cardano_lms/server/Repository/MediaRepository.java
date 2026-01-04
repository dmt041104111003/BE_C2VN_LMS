package com.cardano_lms.server.Repository;


import com.cardano_lms.server.Entity.Media;
import com.cardano_lms.server.constant.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByType(MediaType type);
    Media findByPublicId(String publicId);
}
