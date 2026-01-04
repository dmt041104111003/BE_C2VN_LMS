package com.cardano_lms.server.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyEnrollmentResponse {
    Long enrollmentId;
    String courseId;
    String courseSlug;
    String courseTitle;
    String courseImage;
    String instructorName;
    LocalDateTime enrolledAt;
    int progressPercent;
    boolean completed;
    int completedLectures;
    String walletAddress;
}

