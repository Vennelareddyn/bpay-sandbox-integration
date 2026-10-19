package com.example.bpay_integration.integration.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class GeneratedBpayAuthService {

    private static final Logger log =
            LoggerFactory.getLogger(GeneratedBpayAuthService.class);

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public GeneratedBpayAuthService(
            @Value("${bpay.generated.auth.token-url}") String tokenUrl,
            @Value("${bpay.generated.auth.client-id}") String clientId,
            @Value("${bpay.generated.auth.client-secret}") String clientSecret) {

        this.restClient = RestClient.builder()
                .baseUrl(tokenUrl)
                .build();

        this.clientId = clientId;
        this.clientSecret = clientSecret;

        log.info("BPAY OAuth client initialized");
    }

    public String generateToken(String scope) {

        long startTime = System.nanoTime();

        log.info("Requesting BPAY OAuth access token for scope: {}", scope);

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("scope", scope);

            Map<?, ?> response = restClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            if (response == null || response.get("access_token") == null) {
                log.error(
                        "BPAY OAuth response did not contain an access token. "
                                + "Scope: {}, Duration: {} ms",
                        scope,
                        elapsedMillis(startTime));

                throw new IllegalStateException(
                        "BPAY OAuth response did not contain an access token");
            }

            String accessToken = response.get("access_token").toString();

            log.info(
                    "BPAY OAuth token generated successfully. "
                            + "Scope: {}, Duration: {} ms",
                    scope,
                    elapsedMillis(startTime));

            return accessToken;

        } catch (RuntimeException ex) {
            log.error(
                    "BPAY OAuth token request failed. "
                            + "Scope: {}, Exception: {}, Duration: {} ms",
                    scope,
                    ex.getClass().getSimpleName(),
                    elapsedMillis(startTime));

            throw ex;
        }
    }

    private long elapsedMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
