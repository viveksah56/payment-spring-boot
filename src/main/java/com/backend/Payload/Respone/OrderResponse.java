package com.backend.Payload.Respone;

import com.backend.Enum.OrderStatus;
import com.backend.Enum.PaymentProvider;
import com.backend.Enum.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderResponse {

    public record ProductResponse(
            UUID productId,
            String name,
            String description,
            Double price,
            Double discount,
            Double effectivePrice,
            Integer stock
    ) {}

    public record OrderItemResponse(
            UUID orderItemId,
            UUID productId,
            String productName,
            Integer quantity,
            Double unitPrice,
            Double discountApplied,
            Double lineTotal
    ) {}

    public record OrderSummaryResponse(
            UUID orderId,
            List<OrderItemResponse> items,
            Double subtotal,
            Double discountAmount,
            Double total,
            OrderStatus status,
            Instant createdAt
    ) {}

    public record CheckoutResponse(
            UUID orderId,
            UUID paymentId,
            String providerOrderId,
            String clientSecret,
            Double amount,
            String currency,
            PaymentProvider provider,
            PaymentStatus paymentStatus,
            OrderStatus orderStatus
    ) {}

    public record PaymentHistoryResponse(
            UUID paymentId,
            UUID orderId,
            Double amount,
            String currency,
            PaymentProvider provider,
            PaymentStatus status,
            String providerOrderId,
            Instant createdAt
    ) {}
}