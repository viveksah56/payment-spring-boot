package com.backend.Payload.Respone;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        Instant timestamp,
        boolean success,
        String message,
        T data,
        int statusCode,
        Map<String, String> errors
) {

    public static <T> ApiResponse<T> success(T data) {
        return build(data, "Request successful", HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return build(data, message, HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> success(T data, String message, HttpStatus status) {
        return build(data, message, status.value());
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .success(false)
                .message(message)
                .statusCode(status.value())
                .errors(Map.of("error", message))
                .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode, Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> validationError(Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .success(false)
                .message("Validation failed")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .build();
    }

    private static <T> ApiResponse<T> build(T data, String message, int statusCode) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .success(true)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .build();
    }
}