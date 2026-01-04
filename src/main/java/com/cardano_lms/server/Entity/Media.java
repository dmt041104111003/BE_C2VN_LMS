package com.cardano_lms.server.Entity;

import com.cardano_lms.server.constant.MediaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;

    String description;

    String location;

    Integer orderIndex;

    String link;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MediaType type;

    @Column(nullable = false)
    String url;

    @Column(nullable = false, unique = true)
    String publicId;

    @Builder.Default
    LocalDateTime updateAt = LocalDateTime.now();
}
