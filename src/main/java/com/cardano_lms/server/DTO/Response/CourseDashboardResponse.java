package com.cardano_lms.server.DTO.Response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseDashboardResponse {
    String id;
    String title;
    LocalDateTime createdAt;
    int totalLectures;
    int totalTests;
    List<EnrollmentDashboardResponse> enrollments;
}
