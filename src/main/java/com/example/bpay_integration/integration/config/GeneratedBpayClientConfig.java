package com.example.bpay_integration.integration.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.bpay_integration.generated.biller.api.RetrieveBpayBillerDetailsApi;
import com.example.bpay_integration.generated.biller.invoker.ApiClient;
import com.example.bpay_integration.generated.validation.api.ValidateBpayPaymentApi;

@Configuration
public class GeneratedBpayClientConfig {

    private static final Logger log =
            LoggerFactory.getLogger(GeneratedBpayClientConfig.class);

    private static final String BPAY_BASE_URL =
            "https://sandbox.api.bpaygroup.com.au/payments/v1";

    @Bean
    public ApiClient generatedBillerApiClient() {
        log.info("Configuring BPAY biller API client");

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(BPAY_BASE_URL);

        log.info("BPAY biller API client configured. Base URL: {}",
                BPAY_BASE_URL);

        return apiClient;
    }

    @Bean
    public com.example.bpay_integration.generated.validation.invoker.ApiClient
    generatedValidationApiClient() {

        log.info("Configuring BPAY payment validation API client");

        var apiClient =
                new com.example.bpay_integration.generated.validation.invoker.ApiClient();

        apiClient.setBasePath(BPAY_BASE_URL);

        log.info("BPAY payment validation API client configured. Base URL: {}",
                BPAY_BASE_URL);

        return apiClient;
    }

    @Bean
    public RetrieveBpayBillerDetailsApi generatedBillerDetailsApi(
            ApiClient generatedBillerApiClient) {

        log.debug("Creating RetrieveBpayBillerDetailsApi");

        RetrieveBpayBillerDetailsApi api =
                new RetrieveBpayBillerDetailsApi(generatedBillerApiClient);

        log.info("RetrieveBpayBillerDetailsApi initialized successfully");

        return api;
    }

    @Bean
    public ValidateBpayPaymentApi generatedValidatePaymentApi(
            com.example.bpay_integration.generated.validation.invoker.ApiClient
                    generatedValidationApiClient) {

        log.debug("Creating ValidateBpayPaymentApi");

        ValidateBpayPaymentApi api =
                new ValidateBpayPaymentApi(generatedValidationApiClient);

        log.info("ValidateBpayPaymentApi initialized successfully");

        return api;
    }
}
