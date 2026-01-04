package com.cardano_lms.server.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchMintRequest {
    private String courseId;
    private List<BatchMintItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchMintItem {
        private Long enrollmentId;
        private String studentName;
        private String studentWalletAddress;
    }
}

