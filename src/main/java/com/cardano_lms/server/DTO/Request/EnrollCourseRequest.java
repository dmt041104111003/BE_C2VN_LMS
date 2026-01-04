package com.cardano_lms.server.DTO.Request;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollCourseRequest {
    @NonNull
    private String userId;
    @NonNull
    private String courseId;
    private String senderAddress;

    private String coursePaymentMethodId;
    private double priceAda;
    private String txHash;
}
