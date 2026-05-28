package com.backend.Service.Impl;

import com.backend.Entity.Role;
import com.backend.Entity.User;
import com.backend.Enum.RoleType;
import com.backend.Mapper.UserMapper;
import com.backend.Payload.Request.AuthRequest.LoginRequest;
import com.backend.Payload.Request.AuthRequest.RegisterRequest;
import com.backend.Payload.Respone.AuthResponse;
import com.backend.Payload.Respone.AuthResponse.LoginResponse;
import com.backend.Payload.Respone.UserResponse;
import com.backend.Repository.RoleRepository;
import com.backend.Repository.UserRepository;
import com.backend.Service.AuthService;
import com.backend.Service.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String accessToken  = jwtService.generateAccessToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication, request.rememberMe());

        int cookieMaxAge = resolveMaxAge(request.rememberMe());
        attachRefreshCookie(response, refreshToken, cookieMaxAge);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.email()));

        log.info("Login successful for '{}'", request.email());
        return new LoginResponse(accessToken, jwtService.getPrefix(), jwtService.getExpiration(), buildUserInfo(user));
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(RoleType.USER)
                                .description("Default user role")
                                .build()
                ));

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .roles(Set.of(userRole))
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {
        String newAccessToken = jwtService.refreshAccessToken(refreshToken);
        boolean rememberMe    = jwtService.extractRememberMe(refreshToken);
        String username       = jwtService.extractUsername(refreshToken);

        attachRefreshCookie(response, refreshToken, resolveMaxAge(rememberMe));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        log.info("Token refreshed for '{}'", username);
        return new LoginResponse(newAccessToken, jwtService.getPrefix(), jwtService.getExpiration(), buildUserInfo(user));
    }

    @Override
    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("Refresh token cookie cleared");
    }

    private int resolveMaxAge(boolean rememberMe) {
        return (int) (rememberMe
                ? jwtService.getRememberMeExpiration()
                : jwtService.getRefreshTokenExpiration()) / 1000;
    }

    private void attachRefreshCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private AuthResponse.UserInfo buildUserInfo(User user) {
        Set<RoleType> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new AuthResponse.UserInfo(user.getUserId(), user.getFullName(), user.getEmail(), roles);
    }
}