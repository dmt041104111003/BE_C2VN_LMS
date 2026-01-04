package com.cardano_lms.server.Service.payment;

import com.cardano_lms.server.DTO.Request.PaymentOptionRequest;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.CoursePaymentMethod;
import com.cardano_lms.server.Entity.PaymentMethod;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.PaymentMethodRepository;
import com.cardano_lms.server.constant.Currency;
import com.cardano_lms.server.constant.PredefinedPaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class FreeEnrollPaymentStrategy implements PaymentMethodStrategy {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public boolean supports(String methodName) {
        return PredefinedPaymentMethod.FREE_ENROLL.equalsIgnoreCase(methodName)
                || "FREE".equalsIgnoreCase(methodName);
    }

    @Override
    public void apply(Course course, PaymentOptionRequest option) {
        String methodKey = PredefinedPaymentMethod.FREE_ENROLL;
        if (!paymentMethodRepository.existsByName(methodKey) && paymentMethodRepository.existsByName("FREE")) {
            methodKey = "FREE";
        }
        PaymentMethod method = paymentMethodRepository.findByName(methodKey)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        CoursePaymentMethod cpm = CoursePaymentMethod.builder()
                .course(course)
                .paymentMethod(method)
                .receiverAddress("")
                .build();

        course.getCoursePaymentMethods().add(cpm);
        course.setDiscount(0.0);
        course.setCurrency(Currency.ADA);
        course.setDiscountEndTime(null);
        course.setPrice(0);
    }
}
