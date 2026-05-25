package com.backend.Service;

import com.backend.Payload.Request.OrderRequest.CheckoutRequest;
import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Respone.OrderResponse.*;
import com.backend.Payload.Respone.PaginationResponse;

import java.util.UUID;

public interface OrderService {
    CheckoutResponse checkout(CheckoutRequest request, String userEmail);
    PaginationResponse<OrderSummaryResponse> getUserOrders(String userEmail, PaginationRequest request);
    OrderSummaryResponse getOrder(UUID orderId, String userEmail);
    PaginationResponse<PaymentHistoryResponse> getUserPayments(String userEmail, PaginationRequest request);
}