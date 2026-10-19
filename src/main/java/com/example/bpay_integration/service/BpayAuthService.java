
package com.example.bpay_integration.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class BpayAuthService {

    private final RestClient restClient;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper objectMapper;

    public BpayAuthService(
            @Value("${bpay.auth.token-url}") String tokenUrl,
            @Value("${bpay.auth.client-id}") String clientId,
            @Value("${bpay.auth.client-secret}") String clientSecret,
            ObjectMapper objectMapper) {

        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public String generateToken(String scope) {

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("grant_type", "client_credentials");
        form.add("scope", scope);

        // Request a fresh token from AP+ on every call.
        String response = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(headers ->
                        headers.setBasicAuth(clientId, clientSecret))
                .body(form)
                .retrieve()
                .body(String.class);

        if (response == null || response.isBlank()) {
            throw new IllegalStateException(
                    "AP+ returned an empty token response");
        }

        try {
            // Parse the JSON response.
            JsonNode json = objectMapper.readTree(response);



            // Extract and return the access token.
            String accessToken =
                    json.path("access_token").asText("");

            if (accessToken.isBlank()) {
                throw new IllegalStateException(
                        "AP+ response does not contain an access_token");
            }

            return accessToken;

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not parse or save AP+ token response", ex);
        }
    }
}
