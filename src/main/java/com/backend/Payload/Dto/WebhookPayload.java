package com.backend.Payload.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebhookPayload {

    public record StripeWebhookPayload(
            String id,
            String type,
            @JsonProperty("api_version") String apiVersion,
            Object data
    ) {}

    public record RazorpayVerificationPayload(
            @JsonProperty("razorpay_order_id") String razorpayOrderId,
            @JsonProperty("razorpay_payment_id") String razorpayPaymentId,
            @JsonProperty("razorpay_signature") String razorpaySignature
    ) {}
}