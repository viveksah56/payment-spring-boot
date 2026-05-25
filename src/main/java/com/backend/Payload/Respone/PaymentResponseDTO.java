package com.backend.Payload.Respone;

public record PaymentResponseDTO(
        String internalPaymentId,
        String providerOrderId,
        String clientSecret,
        Double amount,
        String currency,
        String status,
        String provider,
        String customerEmail
) {}

