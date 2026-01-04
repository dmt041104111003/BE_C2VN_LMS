package com.cardano_lms.server.Service;

import com.cardano_lms.server.Exception.AppException;
import com.cardano_lms.server.Exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Service
@RequiredArgsConstructor
public class PinataService {

    private static final String PIN_FILE_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";
    private static final String UNPIN_FILE_URL = "https://api.pinata.cloud/pinning/unpinFileFromIPFS";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${PINATA_JWT:}")
    private String pinataJwt;

    @Value("${PINATA_API_KEY:}")
    private String pinataApiKey;

    @Value("${PINATA_SECRET_KEY:}")
    private String pinataApiSecret;

    public boolean isConfigured() {
        return hasJwt() || hasKeyPair();
    }

    private boolean hasJwt() {
        return pinataJwt != null && !pinataJwt.isBlank();
    }

    private boolean hasKeyPair() {
        return (pinataApiKey != null && !pinataApiKey.isBlank())
                && (pinataApiSecret != null && !pinataApiSecret.isBlank());
    }

    private HttpHeaders buildHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        if (hasJwt()) {
            headers.setBearerAuth(pinataJwt);
        } else {
            headers.add("pinata_api_key", pinataApiKey);
            headers.add("pinata_secret_api_key", pinataApiSecret);
        }
        return headers;
    }

    private String executeUpload(Resource resource) throws Exception {
        validateConfiguration();
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, buildHeaders(MediaType.MULTIPART_FORM_DATA));

        try {
            ResponseEntity<String> response = new RestTemplate().postForEntity(PIN_FILE_URL, requestEntity, String.class);
            return parseIpfsHash(response);
        } catch (RestClientException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validateConfiguration() {
        if (!isConfigured()) {
            throw new AppException(ErrorCode.MISSING_ARGUMENT);
        }
    }

    private String parseIpfsHash(ResponseEntity<String> response) throws Exception {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        JsonNode json = MAPPER.readTree(response.getBody());
        String hash = json.path("IpfsHash").asText();
        if (hash == null || hash.isBlank()) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        return hash;
    }

    public String uploadFileToIpfs(MultipartFile file) throws Exception {
        return executeUpload(new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
    }

    public String uploadBytesToIpfs(byte[] data, String filename) throws Exception {
        return executeUpload(new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
    }

    public String uploadFileToIpfs(java.io.File file) throws Exception {
        return executeUpload(new FileSystemResource(file));
    }

    public boolean deleteFileFromIpfs(@NonNull String ipfsHash) {
        validateConfiguration();
        HttpEntity<String> requestEntity = new HttpEntity<>(null, buildHeaders(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response = new RestTemplate().exchange(
                    UNPIN_FILE_URL + "/" + ipfsHash,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
