package com.backend.Service.Impl;

import com.backend.Payload.Request.PaymentRequestDTO;
import com.backend.Payload.Respone.PaymentResponseDTO;
import com.backend.Service.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service("codGateway")
public class CodPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        String codOrderId = "COD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("COD order created: {}", codOrderId);

        return new PaymentResponseDTO(
                null,
                codOrderId,
                null,
                request.amount(),
                request.currency(),
                "PENDING",
                "COD",
                request.customerEmail()
        );
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