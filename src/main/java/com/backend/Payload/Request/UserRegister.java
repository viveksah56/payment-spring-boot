package com.backend.Payload.Request;


public record UserRegister(
        String fullName,
        String email,
        String password
) {
}
