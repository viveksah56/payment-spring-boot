package com.backend.Service;

import com.backend.Payload.Request.PaymentRequestDTO;
import com.backend.Payload.Respone.PaymentResponseDTO;

public interface PaymentGateway {
    PaymentResponseDTO createPayment(PaymentRequestDTO request);

    PaymentResponseDTO getPaymentStatus(String providerOrderId);

    PaymentResponseDTO refundPayment(String providerPaymentId, Double amount);
}
