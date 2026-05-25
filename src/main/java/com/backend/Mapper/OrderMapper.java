package com.backend.Mapper;

import com.backend.Entity.Order;
import com.backend.Entity.OrderItem;
import com.backend.Entity.Payment;
import com.backend.Payload.Respone.OrderResponse.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getOrderItemId(),
                item.getProduct().getProductId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountApplied(),
                item.getLineTotal()
        );
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();
        return new OrderSummaryResponse(
                order.getOrderId(),
                items,
                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    public CheckoutResponse toCheckoutResponse(Order order, Payment payment) {
        return new CheckoutResponse(
                order.getOrderId(),
                payment.getPaymentId(),
                payment.getProviderOrderId(),
                payment.getClientSecret(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getStatus(),
                order.getStatus()
        );
    }

    public PaymentHistoryResponse toPaymentHistoryResponse(Payment payment) {
        return new PaymentHistoryResponse(
                payment.getPaymentId(),
                payment.getOrder().getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProvider(),
                payment.getStatus(),
                payment.getProviderOrderId(),
                payment.getCreatedAt()
        );
    }
}