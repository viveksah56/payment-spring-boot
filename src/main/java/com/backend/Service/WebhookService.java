package com.backend.Service;

public interface WebhookService {
    void handleStripeWebhook(String payload, String sigHeader);
    void handleRazorpayWebhook(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
}