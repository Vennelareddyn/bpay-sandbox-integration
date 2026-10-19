
package com.example.bpay_integration.dto;

public record TokenStatusResponse(
        boolean authenticated,
        String message
) {}
