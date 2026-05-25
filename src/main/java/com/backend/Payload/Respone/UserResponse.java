package com.backend.Payload.Respone;



import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String fullName,
        String email,
        Instant createdAt,
        Instant updatedAt
) {
}
