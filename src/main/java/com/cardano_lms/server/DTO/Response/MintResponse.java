package com.cardano_lms.server.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MintResponse {
    private String txHash;
    private String policyId;
    private String assetName;
}

