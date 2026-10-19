
package com.example.bpay_integration.controller;

import com.example.bpay_integration.dto.PaymentValidationRequest;
import com.example.bpay_integration.service.BpayService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/bpay")
public class BpayController {

    private final BpayService bpayService;

    public BpayController(BpayService bpayService) {
        this.bpayService = bpayService;
    }

    @GetMapping("/biller/{billerCode}")
    public ResponseEntity<String> getBillerDetails(
            @PathVariable String billerCode) {

        try {
            String response =
                    bpayService.getBillerDetails(billerCode);

            return ResponseEntity.ok(response);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(502)
                    .body(ex.getResponseBodyAsString());
        }
    }

    @PostMapping(
            value = "/validatepayments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> validatePayments(
            @RequestBody PaymentValidationRequest request) {

        try {
            String response =
                    bpayService.validatePayments(request);

            return ResponseEntity.ok(response);

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(502)
                    .body(ex.getResponseBodyAsString());
        }
    }
}
