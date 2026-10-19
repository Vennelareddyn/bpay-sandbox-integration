
package com.example.bpay_integration.service;

import com.example.bpay_integration.dto.PaymentValidationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Service
public class BpayService {

    private static final Logger log =
            LoggerFactory.getLogger(BpayService.class);

    private final BpayAuthService bpayAuthService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public BpayService(
            BpayAuthService bpayAuthService,
            @Value("${bpay.api.base-url}") String baseUrl,
            ObjectMapper objectMapper) {

        this.bpayAuthService = bpayAuthService;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    // API 1: Retrieve details for a single biller
    public String getBillerDetails(String billerCode) {

        String token =
                bpayAuthService.generateToken("read:validate-biller");

        String path = "/biller/" + billerCode;

        log.info("========== BPAY OUTGOING REQUEST ==========");
        log.info("Method: GET");
        log.info("URL: {}{}", restClientBaseUrl(), path);
        log.info("Authorization: Bearer [MASKED]");
        log.info("Request body: <none>");

        return restClient.get()
                .uri("/biller/{billerCode}", billerCode)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .exchange((request, response) -> {

                    String responseBody = new String(
                            response.getBody().readAllBytes(),
                            StandardCharsets.UTF_8);

                    log.info("========== BPAY INCOMING RESPONSE ==========");
                    log.info("HTTP status: {}", response.getStatusCode());
                    log.info("Response body:\n{}", responseBody);

                    if (response.getStatusCode().isError()) {
                        throw new RestClientResponseException(
                                "BPAY biller request failed",
                                response.getStatusCode().value(),
                                response.getStatusText(),
                                response.getHeaders(),
                                responseBody.getBytes(
                                        StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8);
                    }

                    return responseBody;
                });
    }

    // API 2: Validate one or more payments

    public String validatePayments(PaymentValidationRequest request) {

        String token =
                bpayAuthService.generateToken("read:validate-payment");

        // Convert the DTO into a JSON tree.
        tools.jackson.databind.JsonNode requestNode =
                objectMapper.valueToTree(request);


        if (requestNode instanceof tools.jackson.databind.node.ObjectNode root
                && root.get("payments") instanceof
                tools.jackson.databind.node.ArrayNode payments) {

            for (tools.jackson.databind.JsonNode item : payments) {
                if (item instanceof tools.jackson.databind.node.ObjectNode paymentItem
                        && paymentItem.get("payment") instanceof
                        tools.jackson.databind.node.ObjectNode payment) {

                    String[] optionalFields = {
                            "settlementDate",
                            "paymentDate",
                            "instructionType"
                    };

                    for (String field : optionalFields) {
                        tools.jackson.databind.JsonNode value = payment.get(field);

                        if (value != null && value.isNull()) {
                            payment.remove(field);
                        }
                    }
                }
            }
        }


        String requestJson = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(requestNode);

        log.info("========== BPAY VALIDATION REQUEST ==========");
        log.info("Method: POST");
        log.info("URL: {}/validatepayments", restClientBaseUrl());
        log.info("Content-Type: application/json");
        log.info("Authorization: Bearer [MASKED]");


        return restClient.post()
                .uri("/validatepayments")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(requestJson)
                .exchange((httpRequest, response) -> {

                    String responseBody = new String(
                            response.getBody().readAllBytes(),
                            StandardCharsets.UTF_8);

                    log.info("========== BPAY VALIDATION RESPONSE ==========");
                    log.info("HTTP status: {}", response.getStatusCode());


                    if (response.getStatusCode().isError()) {
                        throw new RestClientResponseException(
                                "BPAY payment validation failed",
                                response.getStatusCode().value(),
                                response.getStatusText(),
                                response.getHeaders(),
                                responseBody.getBytes(StandardCharsets.UTF_8),
                                StandardCharsets.UTF_8);
                    }

                    return responseBody;
                });
    }


    private String restClientBaseUrl() {
        return "https://sandbox.api.bpaygroup.com.au/payments/v1";
    }
}
