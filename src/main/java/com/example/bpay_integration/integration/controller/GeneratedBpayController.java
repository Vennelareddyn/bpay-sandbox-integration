package com.example.bpay_integration.integration.controller;

import com.example.bpay_integration.generated.biller.model.GetBillersBillercode200Response;
import com.example.bpay_integration.generated.validation.model.PostValidatepayments200Response;
import com.example.bpay_integration.generated.validation.model.PostValidatepaymentsRequest;
import com.example.bpay_integration.integration.service.GeneratedBpayService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bpay-generated")
public class GeneratedBpayController {

    private static final Logger log =
            LoggerFactory.getLogger(GeneratedBpayController.class);

    private final GeneratedBpayService generatedBpayService;

    public GeneratedBpayController(
            GeneratedBpayService generatedBpayService) {
        this.generatedBpayService = generatedBpayService;
    }

    @GetMapping(
            value = "/biller/{billerCode}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetBillersBillercode200Response> getBillerDetails(
            @PathVariable String billerCode) {

        log.info("Received request to retrieve BPAY biller details");

        try {
            GetBillersBillercode200Response response =
                    generatedBpayService.getBillerDetails(billerCode);

            log.info("BPAY biller details request completed successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            log.error(
                    "Failed to retrieve BPAY biller details. Exception type: {}",
                    ex.getClass().getSimpleName());

            throw ex;
        }
    }

    @PostMapping(
            value = "/validatepayments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostValidatepayments200Response> validatePayments(
            @RequestBody PostValidatepaymentsRequest request) {

        log.info("Received BPAY payment validation request");

        try {
            PostValidatepayments200Response response =
                    generatedBpayService.validatePayments(request);

            log.info("BPAY payment validation request completed successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            log.error(
                    "BPAY payment validation failed. Exception type: {}",
                    ex.getClass().getSimpleName());

            throw ex;
        }
    }
}
