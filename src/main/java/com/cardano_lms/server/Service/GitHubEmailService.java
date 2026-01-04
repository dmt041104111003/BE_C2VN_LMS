package com.cardano_lms.server.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class GitHubEmailService {

    private final ObjectMapper objectMapper;

    private static final String GITHUB_EMAILS_API = "https://api.github.com/user/emails";

    public String getPrimaryEmail(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        
        try {

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(GITHUB_EMAILS_API))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode emails = objectMapper.readTree(response.body());

            for (JsonNode emailNode : emails) {
                boolean isPrimary = emailNode.path("primary").asBoolean(false);
                boolean isVerified = emailNode.path("verified").asBoolean(false);
                String email = emailNode.path("email").asText(null);

                if (isPrimary && isVerified && email != null) {
                    return email;
                }
            }

            for (JsonNode emailNode : emails) {
                boolean isVerified = emailNode.path("verified").asBoolean(false);
                String email = emailNode.path("email").asText(null);

                if (isVerified && email != null && !email.contains("noreply.github.com")) {
                    return email;
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }
}
