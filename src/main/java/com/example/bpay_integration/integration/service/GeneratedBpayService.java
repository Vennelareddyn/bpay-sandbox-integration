package com.example.bpay_integration.integration.service;

import com.example.bpay_integration.generated.biller.api.RetrieveBpayBillerDetailsApi;
import com.example.bpay_integration.generated.biller.model.GetBillersBillercode200Response;
import com.example.bpay_integration.generated.validation.api.ValidateBpayPaymentApi;
import com.example.bpay_integration.generated.validation.invoker.ApiClient;
import com.example.bpay_integration.generated.validation.model.PostValidatepayments200Response;
import com.example.bpay_integration.generated.validation.model.PostValidatepaymentsRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Service
public class GeneratedBpayService {

    private static final Logger log =
            LoggerFactory.getLogger(GeneratedBpayService.class);

    private static final String BILLER_SCOPE = "read:validate-biller";
    private static final String PAYMENT_SCOPE = "read:validate-payment";

    private final GeneratedBpayAuthService authService;
    private final RetrieveBpayBillerDetailsApi billerApi;
    private final ValidateBpayPaymentApi validationApi;

    public GeneratedBpayService(
            GeneratedBpayAuthService authService,
            RetrieveBpayBillerDetailsApi billerApi,
            ValidateBpayPaymentApi validationApi) {

        this.authService = authService;
        this.billerApi = billerApi;
        this.validationApi = validationApi;

        ApiClient originalClient = validationApi.getApiClient();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Float.class, ToStringSerializer.instance);

        JsonMapper mapper = JsonMapper.builder()
                .defaultDateFormat(originalClient.getDateFormat())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(module)
                .changeDefaultPropertyInclusion(
                        inclusion -> inclusion.withValueInclusion(
                                JsonInclude.Include.NON_NULL))
                .build();

        ApiClient configuredClient =
                new ApiClient(mapper, originalClient.getDateFormat());

        configuredClient.setBasePath(originalClient.getBasePath());
        configuredClient.setServers(originalClient.getServers());
        configuredClient.setServerIndex(originalClient.getServerIndex());

        validationApi.setApiClient(configuredClient);

        log.info(
                "Generated BPAY service initialized. Validation base URL: {}",
                configuredClient.getBasePath());
    }

    public GetBillersBillercode200Response getBillerDetails(
            String billerCode) {

        long startTime = System.nanoTime();

        log.info("Starting BPAY biller details request");

        try {
            String accessToken = authService.generateToken(BILLER_SCOPE);
            billerApi.getApiClient().setAccessToken(accessToken);

            GetBillersBillercode200Response response =
                    billerApi.getBillersBillercode(billerCode);

            log.info(
                    "BPAY biller details request succeeded. Duration: {} ms",
                    elapsedMillis(startTime));

            return response;

        } catch (RuntimeException ex) {
            log.error(
                    "BPAY biller details request failed. "
                            + "Exception: {}, Duration: {} ms",
                    ex.getClass().getSimpleName(),
                    elapsedMillis(startTime));

            throw ex;
        }
    }

    public PostValidatepayments200Response validatePayments(
            PostValidatepaymentsRequest request) {

        long startTime = System.nanoTime();

        log.info("Starting BPAY payment validation request");

        try {
            String accessToken = authService.generateToken(PAYMENT_SCOPE);
            validationApi.getApiClient().setAccessToken(accessToken);

            log.debug(
                    "Sending BPAY payment validation request to {}",
                    validationApi.getApiClient().getBasePath());

            PostValidatepayments200Response response =
                    validationApi.postValidatepayments(request);

            log.info(
                    "BPAY payment validation succeeded. Duration: {} ms",
                    elapsedMillis(startTime));

            return response;

        } catch (RuntimeException ex) {
            log.error(
                    "BPAY payment validation failed. "
                            + "Exception: {}, Duration: {} ms",
                    ex.getClass().getSimpleName(),
                    elapsedMillis(startTime));

            throw ex;
        }
    }

    private long elapsedMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
