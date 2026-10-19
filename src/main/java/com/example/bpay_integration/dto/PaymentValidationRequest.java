
package com.example.bpay_integration.dto;

import java.util.List;

public record PaymentValidationRequest(
        List<PaymentItem> payments
) {
    public record PaymentItem(
            String tid,
            Payment payment
    ) {}

    public record Payment(
            String billerCode,
            String crn,
            String amount,
            String settlementDate,
            String paymentMethod,
            String paymentDate,
            String instructionType
    ) {}
}
