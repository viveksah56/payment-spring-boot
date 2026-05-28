package com.backend.Service.Impl;

import com.backend.Entity.Order;
import com.backend.Entity.Payment;
import com.backend.Enum.OrderStatus;
import com.backend.Enum.PaymentStatus;
import com.backend.Repository.OrderRepository;
import com.backend.Repository.PaymentRepository;
import com.backend.Service.WebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${payment.stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @Value("${payment.razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            throw new SecurityException("Invalid Stripe webhook signature");
        }

        log.info("Stripe webhook event received: {}", event.getType());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElseThrow();
                updatePaymentStatus(intent.getId(), PaymentStatus.SUCCESS, OrderStatus.SHIPPED);
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElseThrow();
                updatePaymentStatus(intent.getId(), PaymentStatus.FAILED, OrderStatus.CANCELLED);
            }
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }
    }

    @Override
    @Transactional
    public void handleRazorpayWebhook(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        String expectedSignature = computeRazorpaySignature(razorpayOrderId, razorpayPaymentId);

        if (!expectedSignature.equals(razorpaySignature)) {
            log.warn("Razorpay webhook signature mismatch for order: {}", razorpayOrderId);
            throw new SecurityException("Invalid Razorpay webhook signature");
        }

        updatePaymentStatus(razorpayOrderId, PaymentStatus.SUCCESS, OrderStatus.SHIPPED);
    }

    private void updatePaymentStatus(String providerOrderId, PaymentStatus paymentStatus, OrderStatus orderStatus) {
        paymentRepository.findByProviderOrderId(providerOrderId).ifPresentOrElse(payment -> {
            payment.setStatus(paymentStatus);
            paymentRepository.save(payment);

            Order order = payment.getOrder();
            order.setStatus(orderStatus);
            orderRepository.save(order);

            log.info("Payment {} → {}, Order {} → {}",
                    payment.getPaymentId(), paymentStatus,
                    order.getOrderId(), orderStatus);
        }, () -> log.warn("No payment found for providerOrderId: {}", providerOrderId));
    }

    private String computeRazorpaySignature(String razorpayOrderId, String razorpayPaymentId) {
        try {
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to compute Razorpay HMAC signature", e);
        }
    }
}