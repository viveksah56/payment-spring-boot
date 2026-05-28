package com.backend.Payload.Request;

import jakarta.validation.constraints.*;

public class ProductRequest {

    public record CreateProductRequest(
            @NotBlank(message = "Product name is required")
            @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
            String name,

            @Size(max = 2000, message = "Description cannot exceed 2000 characters")
            String description,

            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.01", message = "Price must be greater than 0")
            Double price,

            @DecimalMin(value = "0.0", message = "Discount cannot be negative")
            @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
            Double discount,

            @NotNull(message = "Stock is required")
            @Min(value = 0, message = "Stock cannot be negative")
            Integer stock
    ) {}

    public record UpdateProductRequest(
            @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
            String name,

            @Size(max = 2000, message = "Description cannot exceed 2000 characters")
            String description,

            @DecimalMin(value = "0.01", message = "Price must be greater than 0")
            Double price,

            @DecimalMin(value = "0.0", message = "Discount cannot be negative")
            @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
            Double discount,

            @Min(value = 0, message = "Stock cannot be negative")
            Integer stock
    ) {}

    public record RestockRequest(
            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Restock quantity must be at least 1")
            Integer quantity
    ) {}
}