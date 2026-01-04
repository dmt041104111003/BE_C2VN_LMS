package com.cardano_lms.server.Service.payment;

import com.cardano_lms.server.DTO.Request.PaymentOptionRequest;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.CoursePaymentMethod;
import com.cardano_lms.server.Entity.PaymentMethod;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.cardano_lms.server.Repository.PaymentMethodRepository;
import com.cardano_lms.server.constant.PredefinedPaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CardanoWalletPaymentStrategy implements PaymentMethodStrategy {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public boolean supports(String methodName) {
        return PredefinedPaymentMethod.CARDANO_WALLET.equalsIgnoreCase(methodName);
    }

    @Override
    public void apply(Course course, PaymentOptionRequest option) {
        if (option.getReceiverAddress() == null || option.getReceiverAddress().isBlank()) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
        PaymentMethod method = paymentMethodRepository.findByName(PredefinedPaymentMethod.CARDANO_WALLET)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        CoursePaymentMethod cpm = CoursePaymentMethod.builder()
                .course(course)
                .paymentMethod(method)
                .receiverAddress(option.getReceiverAddress().trim())
                .build();
        course.getCoursePaymentMethods().add(cpm);
    }
}
