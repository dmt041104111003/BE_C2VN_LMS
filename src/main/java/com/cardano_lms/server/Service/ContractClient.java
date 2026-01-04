package com.cardano_lms.server.Service;

import com.cardano_lms.server.DTO.Response.MintResponse;
import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ENDPOINT_MINT = "/mint";
    private static final String ENDPOINT_BATCH_MINT = "/batch-mint";
    private static final String ENDPOINT_BURN = "/burn";

    @Value("${CONTRACT_API_BASE_URL:}")
    private String contractBaseUrl;

    public MintResponse mint(String course, String assetName, Map<String, String> metadata, String receiver, int quantity) {
        validateConfig();
        
        Map<String, Object> body = new HashMap<>();
        body.put("course", course);
        body.put("asset_name", assetName);
        body.put("metadata", metadata);
        body.put("quantity", quantity);
        body.put("receiver", receiver);

        JsonNode json = executePost(ENDPOINT_MINT, body, ErrorCode.CONTRACT_MINT_FAILED);
        String txHash = extractTxHash(json, ErrorCode.CONTRACT_MINT_FAILED);
        
        return MintResponse.builder()
                .txHash(txHash)
                .policyId(json.path("policy_id").asText())
                .assetName(assetName)
                .build();
    }

    public MintResponse batchMint(String course, List<Map<String, Object>> items) {
        validateConfig();
        
        Map<String, Object> body = new HashMap<>();
        body.put("course", course);
        body.put("items", items);

        JsonNode json = executePost(ENDPOINT_BATCH_MINT, body, ErrorCode.CONTRACT_MINT_FAILED);
        String txHash = extractTxHash(json, ErrorCode.CONTRACT_MINT_FAILED);
        
        return MintResponse.builder()
                .txHash(txHash)
                .policyId(json.path("policy_id").asText())
                .build();
    }

    public String burn(String course, String assetName, int quantity) {
        validateConfig();
        
        Map<String, Object> body = new HashMap<>();
        body.put("course", course);
        body.put("asset_name", assetName);
        body.put("quantity", quantity);

        JsonNode json = executePost(ENDPOINT_BURN, body, ErrorCode.CONTRACT_BURN_FAILED);
        return extractTxHash(json, ErrorCode.CONTRACT_BURN_FAILED);
    }

    private void validateConfig() {
        if (contractBaseUrl == null || contractBaseUrl.isBlank()) {
            throw new AppException(ErrorCode.CONTRACT_SERVICE_UNAVAILABLE);
        }
    }

    private JsonNode executePost(String endpoint, Map<String, Object> body, ErrorCode failCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> resp = rest.postForEntity(contractBaseUrl + endpoint, req, String.class);
            
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new AppException(failCode);
            }
            
            return MAPPER.readTree(resp.getBody());
        } catch (RestClientException e) {
            throw new AppException(ErrorCode.CONTRACT_SERVICE_UNAVAILABLE);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(failCode);
        }
    }

    private String extractTxHash(JsonNode json, ErrorCode failCode) {
        String txHash = json.path("tx_hash").asText();
        if (txHash == null || txHash.isBlank()) {
            txHash = json.path("txHash").asText();
        }
        if (txHash == null || txHash.isBlank()) {
            throw new AppException(failCode);
        }
        return txHash;
    }
}
