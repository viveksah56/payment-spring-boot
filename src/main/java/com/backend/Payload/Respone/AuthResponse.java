package com.backend.Payload.Respone;

import com.backend.Enum.RoleType;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;
@Builder
public class AuthResponse {

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserInfo user
    ) {}

    public record UserInfo(
            UUID userId,
            String fullName,
            String email,
            Set<RoleType> roles
    ) {}
}