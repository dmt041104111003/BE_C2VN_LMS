package com.cardano_lms.server.Service.payment;

import com.cardano_lms.server.DTO.Request.PaymentOptionRequest;
import com.cardano_lms.server.Entity.Course;

public interface PaymentMethodStrategy {
    boolean supports(String methodName);

    void apply(Course course, PaymentOptionRequest option);
}
