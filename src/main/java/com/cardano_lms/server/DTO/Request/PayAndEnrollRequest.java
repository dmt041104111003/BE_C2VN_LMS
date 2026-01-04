package com.cardano_lms.server.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayAndEnrollRequest {
    private String courseId;
    private String userId;
    private String transactionHash;
    private String walletAddress;
    private Double amount;
    private String currency;
}
