package com.backend.Service.Impl;

import com.backend.Payload.Request.PaymentRequestDTO;
import com.backend.Payload.Respone.PaymentResponseDTO;
import com.backend.Service.PaymentGateway;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service("razorpayGateway")
public class RazorpayPaymentGateway implements PaymentGateway {

    @Value("${payment.razorpay.key-id:rzp_test_placeholder}")
    private String keyId;

    @Value("${payment.razorpay.secret-key}")
    private String secretKey;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() throws RazorpayException {
        this.razorpayClient = new RazorpayClient(keyId, secretKey);
    }

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        try {
            long amountInPaise = Math.round(request.amount() * 100);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", request.currency().toUpperCase());
            orderRequest.put("receipt", "rcpt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);
            log.info("Razorpay order created: {}", Optional.ofNullable(order.get("id")));

            return new PaymentResponseDTO(
                    null,
                    order.get("id"),
                    null,
                    request.amount(),
                    request.currency(),
                    order.get("status"),
                    "RAZORPAY",
                    request.customerEmail()
            );
        } catch (RazorpayException e) {
            log.error("Razorpay payment failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay payment: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO getPaymentStatus(String providerOrderId) {
        try {
            Order order = razorpayClient.orders.fetch(providerOrderId);
            return new PaymentResponseDTO(
                    null,
                    order.get("id"),
                    null,
                    ((Number) order.get("amount")).doubleValue() / 100.0,
                    order.get("currency"),
                    order.get("status"),
                    "RAZORPAY",
                    null
            );
        } catch (RazorpayException e) {
            log.error("Failed to fetch Razorpay order status: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch Razorpay payment status: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO refundPayment(String providerPaymentId, Double amount) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", Math.round(amount * 100));

            razorpayClient.payments.refund(providerPaymentId, refundRequest);
            log.info("Razorpay refund initiated for payment: {}", providerPaymentId);

            return new PaymentResponseDTO(
                    null,
                    providerPaymentId,
                    null,
                    amount,
                    null,
                    "refunded",
                    "RAZORPAY",
                    null
            );
        } catch (RazorpayException e) {
            log.error("Razorpay refund failed: {}", e.getMessage());
            throw new RuntimeException("Failed to refund Razorpay payment: " + e.getMessage());
        }
    }
}