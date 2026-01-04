package com.cardano_lms.server.DTO.Request;

import lombok.Data;

@Data
public class CertificateRequest {
    private Long enrollmentId;
    private String studentName;
    private String studentWalletAddress;

}
