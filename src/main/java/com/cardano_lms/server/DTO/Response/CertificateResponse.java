package com.cardano_lms.server.DTO.Response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CertificateResponse {
    private Long id;
    private String certificateNumber;
    private LocalDateTime issuedAt;
    private String imgUrl;
    private String assetName;
    private String policyId;
    private String qrUrl;
    private String txHash;

    private Long enrollmentId;
    private String userId;
    private String name;
    private String courseId;
    private String courseTitle;
    private String walletAddress;
}
