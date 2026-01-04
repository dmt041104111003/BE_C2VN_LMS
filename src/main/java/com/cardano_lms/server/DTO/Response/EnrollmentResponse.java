package com.cardano_lms.server.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private String orderId;
    private String userId;
    private String courseId;
    private double price;
    private String paymentMethod;
    private String status;
    private LocalDateTime enrolledAt;
    private boolean completed;
}
