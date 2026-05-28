package com.backend.Controller;

import com.backend.Payload.Dto.WebhookPayload.RazorpayVerificationPayload;
import com.backend.Payload.Respone.ApiResponse;
import com.backend.Service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/stripe")
    public ResponseEntity<ApiResponse<Void>> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        webhookService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok(ApiResponse.success(null, "Stripe webhook processed"));
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<ApiResponse<Void>> razorpayVerify(
            @Valid @RequestBody RazorpayVerificationPayload payload
    ) {
        webhookService.handleRazorpayWebhook(
                payload.razorpayOrderId(),
                payload.razorpayPaymentId(),
                payload.razorpaySignature()
        );
        return ResponseEntity.ok(ApiResponse.success(null, "Razorpay payment verified"));
    }
}