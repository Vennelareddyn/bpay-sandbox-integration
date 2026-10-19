
package com.example.bpay_integration.controller;

import com.example.bpay_integration.dto.TokenStatusResponse;
import com.example.bpay_integration.service.BpayAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/bpay/auth")
public class BpayAuthController {

    private static final Logger log =
            LoggerFactory.getLogger(BpayAuthController.class);

    private final BpayAuthService bpayAuthService;

    public BpayAuthController(BpayAuthService bpayAuthService) {
        this.bpayAuthService = bpayAuthService;
    }

    @GetMapping("/test")
    public ResponseEntity<TokenStatusResponse> testAuthentication() {

        try {
            String token =
                    bpayAuthService.generateToken("read:validate-biller");

            if (token == null || token.isBlank()) {
                return ResponseEntity.internalServerError()
                        .body(new TokenStatusResponse(
                                false, "AP+ returned no access token"));
            }

            return ResponseEntity.ok(
                    new TokenStatusResponse(
                            true, "AP+ authentication succeeded"));

        } catch (RestClientResponseException ex) {
            log.error("AP+ token request failed with HTTP status {}",
                    ex.getStatusCode().value());

            return ResponseEntity.status(502)
                    .body(new TokenStatusResponse(
                            false,
                            "AP+ token request failed. HTTP status: "
                                    + ex.getStatusCode().value()));

        } catch (Exception ex) {
            log.error("Failed to obtain AP+ access token", ex);

            return ResponseEntity.status(502)
                    .body(new TokenStatusResponse(
                            false,
                            "Could not obtain AP+ token. Check application logs."));
        }
    }
}
