package com.backend.Payload.Request;

import com.backend.Enum.PaymentProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public class OrderRequest {

    public record CheckoutRequest(
            @NotEmpty(message = "Cart must have at least one item")
            @Valid
            List<CartItemRequest> items,

            @NotBlank(message = "Currency is required")
            String currency,

            @NotNull(message = "Payment provider is required")
            PaymentProvider provider
    ) {}

    public record CartItemRequest(
            @NotNull(message = "Product ID is required")
            java.util.UUID productId,

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            Integer quantity
    ) {}
}