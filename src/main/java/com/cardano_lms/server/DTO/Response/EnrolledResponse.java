package com.cardano_lms.server.DTO.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrolledResponse {
    Long enrolledId;
    String userId;
    String userName;
    String email;
    String walletAddress;
    LocalDateTime enrollAt;
    int totalLectures;
    int totalTests;
    int lecturesCompleted;
    int testsCompleted;
    int lectureProgressPercent;
    boolean allLecturesCompleted;
    boolean allTestsCompleted;
    boolean courseCompleted;
    boolean hasCertificate;
}
