package com.backend.Service.Impl;

import com.backend.Payload.Request.PaymentRequestDTO;
import com.backend.Payload.Respone.PaymentResponseDTO;
import com.backend.Service.PaymentGateway;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("stripeGateway")
public class StripePaymentGateway implements PaymentGateway {

    @Value("${payment.stripe.secret-key}")
    private String secretKey;

    private StripeClient stripeClient;

    @PostConstruct
    public void init() {
        this.stripeClient = new StripeClient(secretKey);
    }

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        try {
            long amountSmallestUnit = Math.round(request.amount() * 100);
            PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                    .setAmount(amountSmallestUnit)
                    .setCurrency(request.currency().toLowerCase())
                    .setReceiptEmail(request.customerEmail())
                    .addPaymentMethodType("card")
                    .build();

            PaymentIntent paymentIntent = stripeClient.v1().paymentIntents().create(createParams);
            log.info("Stripe PaymentIntent created: {}", paymentIntent.getId());


            return new PaymentResponseDTO(
                    null,
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    request.amount(),
                    request.currency(),
                    paymentIntent.getStatus(),
                    "STRIPE",
                    request.customerEmail()
            );
        } catch (StripeException | RuntimeException e) {
            log.error("Stripe payment failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create Stripe payment: " + e.getMessage());
            // TODO: handle exception
        }
       
    }

    @Override
    public PaymentResponseDTO getPaymentStatus(String providerOrderId) {
        return null;
    }

    @Override
    public PaymentResponseDTO refundPayment(String providerPaymentId, Double amount) {
        return null;
    }
}
