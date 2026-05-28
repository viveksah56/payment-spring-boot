package com.backend.Service;

import com.backend.Payload.Request.AuthRequest.LoginRequest;
import com.backend.Payload.Request.AuthRequest.RegisterRequest;
import com.backend.Payload.Respone.AuthResponse.LoginResponse;
import com.backend.Payload.Respone.UserResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request, HttpServletResponse response);
    UserResponse register(RegisterRequest request);
    LoginResponse refresh(String refreshToken, HttpServletResponse response);
    void logout(HttpServletResponse response);
}