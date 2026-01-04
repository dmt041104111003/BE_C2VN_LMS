package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.constant.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {
    private LocalDateTime enrolledAt;
    private boolean completed;
    private String coursePaymentMethodName;
    private OrderStatus status;
    private String orderId;
    private double price;
    private String courseTitle;
    private String imageUrl;
}
