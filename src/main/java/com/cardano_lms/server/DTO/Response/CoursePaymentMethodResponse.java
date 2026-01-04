package com.cardano_lms.server.DTO.Response;

import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePaymentMethodResponse {
    private Long id;
    private PaymentMethod paymentMethod;
    private String receiverAddress;
}
