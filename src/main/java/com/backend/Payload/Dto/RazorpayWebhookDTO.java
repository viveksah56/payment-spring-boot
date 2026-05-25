package com.backend.Payload.Dto;


/**
 * Razorpay webhook verification payload.
 * Razorpay sends razorpay_order_id + razorpay_payment_id + razorpay_signature
 * which we must verify using HMAC-SHA256.
 */
public record RazorpayWebhookDTO(
        String razorpayOrderId,
        String razorpayPaymentId,
        String razorpaySignature
) {}