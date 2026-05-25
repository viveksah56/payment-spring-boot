package com.backend.Controller;

import com.backend.Payload.Request.OrderRequest.CheckoutRequest;
import com.backend.Payload.Request.PaginationRequest;
import com.backend.Payload.Respone.ApiResponse;
import com.backend.Payload.Respone.OrderResponse.*;
import com.backend.Payload.Respone.PaginationResponse;
import com.backend.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CheckoutResponse response = orderService.checkout(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Order placed successfully", HttpStatus.CREATED));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginationResponse<OrderSummaryResponse>>> getMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PaginationRequest request = new PaginationRequest(page, size, null, sort, sortDirection);
        PaginationResponse<OrderSummaryResponse> orders = orderService.getUserOrders(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderSummaryResponse>> getOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        OrderSummaryResponse order = orderService.getOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaginationResponse<PaymentHistoryResponse>>> getMyPayments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PaginationRequest request = new PaginationRequest(page, size, null, sort, sortDirection);
        PaginationResponse<PaymentHistoryResponse> payments = orderService.getUserPayments(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}