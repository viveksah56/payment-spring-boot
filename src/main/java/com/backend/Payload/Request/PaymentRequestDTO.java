package com.backend.Payload.Request;

import com.backend.Enum.PaymentProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record PaymentRequestDTO(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        Double amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Payment provider is required")
        PaymentProvider provider,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Invalid email format")
        String customerEmail,

        // Optional — link payment to an existing user
        UUID userId
) {}