package com.cardano_lms.server.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BlockchainVerificationService {

    @Value("${BLOCKFROST_PROJECT_ID}")
    private String blockfrostProjectId;

    @Value("${BLOCKFROST_API}")
    private String blockfrostApi;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean verifyNftOwnership(String walletAddress, String policyId, String assetName) {
        if (blockfrostProjectId == null || blockfrostProjectId.isBlank() || 
            blockfrostApi == null || blockfrostApi.isBlank()) {
            return false;
        }

        try {
            String baseUrl = blockfrostApi.endsWith("/") ? blockfrostApi.substring(0, blockfrostApi.length() - 1) : blockfrostApi;
            String url = baseUrl + "/addresses/" + walletAddress;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("project_id", blockfrostProjectId);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return false;
            }

            String assetNameHex = bytesToHex(assetName.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String assetUnitSimple = policyId + assetNameHex;
            String assetUnitCip68 = policyId + "000de140" + assetNameHex;
            
            List<Map<String, Object>> amounts = (List<Map<String, Object>>) response.getBody().get("amount");
            
            if (amounts == null) return false;

            for (Map<String, Object> amount : amounts) {
                String unit = (String) amount.get("unit");
                if (assetUnitSimple.equals(unit) || assetUnitCip68.equals(unit) || unit.endsWith(assetNameHex)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
