package com.cardano_lms.server.DTO.Response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrollmentDashboardResponse {
    Long id;
    LocalDateTime enrolledAt;
    boolean completed;
    double price;
    UserDashboardResponse user;
    ProgressDashboardResponse progressData;
}
