
package com.allypay.fineract_integration.exception;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 1. Handle request validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 400);
        response.put("message", "Validation failed");
        response.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // 2. Handle errors returned by Apache Fineract
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleFineractError(
            RestClientResponseException ex
    ) {
        int upstreamStatus = ex.getStatusCode().value();

        log.warn(
                "Fineract request rejected. Upstream HTTP status: {}",
                upstreamStatus
        );

        // Recognize insufficient funds from Fineract's error codes
        if (isInsufficientFunds(ex)) {
            Map<String, Object> response = new LinkedHashMap<>();

            response.put("status", 409);
            response.put("code", "INSUFFICIENT_FUNDS");
            response.put(
                    "message",
                    "Insufficient funds to complete withdrawal"
            );

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        int responseStatus = switch (upstreamStatus) {
            case 400, 404, 409, 422 -> upstreamStatus;
            default -> 502;
        };

        String message = switch (responseStatus) {
            case 400 -> "Transaction rejected by core banking system";
            case 404 -> "Requested banking resource not found";
            case 409 -> "Banking operation conflicts with current account state";
            case 422 -> "Banking operation could not be processed";
            default -> "Core banking service error";
        };

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", responseStatus);
        response.put("message", message);

        return ResponseEntity
                .status(responseStatus)
                .body(response);
    }

    // 3. Handle Fineract connection failures
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleFineractUnavailable(
            ResourceAccessException ex
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 503);
        response.put("message", "Core banking service unavailable");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // 4. Identify insufficient-funds error codes
    private boolean isInsufficientFunds(
            RestClientResponseException ex
    ) {
        try {
            JsonNode root = objectMapper.readTree(
                    ex.getResponseBodyAsString()
            );

            String errorCode = root
                    .path("userMessageGlobalisationCode")
                    .asText("")
                    .toLowerCase(Locale.ROOT);

            if (containsInsufficientFundsCode(errorCode)) {
                return true;
            }

            JsonNode errors = root.path("errors");

            if (errors.isArray()) {
                for (JsonNode error : errors) {
                    String code = error
                            .path("userMessageGlobalisationCode")
                            .asText("")
                            .toLowerCase(Locale.ROOT);

                    if (containsInsufficientFundsCode(code)) {
                        return true;
                    }
                }
            }

        } catch (JacksonException exParsing) {
            log.warn("Unable to parse Fineract error response");
        }

        return false;
    }

    private boolean containsInsufficientFundsCode(String code) {
        return code.contains("insufficient")
                && (code.contains("balance")
                || code.contains("fund"));
    }
}
