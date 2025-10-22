package com.example.OnePieceTheoryEvaluator.util;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class ErrorResponseUtil {

    // Generic error messages that don't expose internal details
    public static final String GENERIC_ERROR_MESSAGE = "Something went wrong. Please try again later.";
    public static final String GENERIC_NOT_FOUND_MESSAGE = "The requested resource was not found.";
    public static final String GENERIC_BAD_REQUEST_MESSAGE = "Invalid request. Please check your input.";
    public static final String GENERIC_UNAUTHORIZED_MESSAGE = "You are not authorized to perform this action.";
    public static final String GENERIC_CONFLICT_MESSAGE = "The operation could not be completed due to a conflict.";
    public static final String GENERIC_VALIDATION_MESSAGE = "Please check your input and try again.";

    /**
     * Creates a generic error response that masks internal error details
     */
    public static ResponseEntity<Response> createGenericErrorResponse(HttpStatus status, String userMessage) {
        Response response = Response.builder()
                .status(status.value())
                .message(userMessage)
                .build();
        return new ResponseEntity<>(response, status);
    }

    /**
     * Creates a generic error response with default message based on status
     */
    public static ResponseEntity<Response> createGenericErrorResponse(HttpStatus status) {
        String message = switch (status) {
            case NOT_FOUND -> GENERIC_NOT_FOUND_MESSAGE;
            case BAD_REQUEST -> GENERIC_BAD_REQUEST_MESSAGE;
            case UNAUTHORIZED -> GENERIC_UNAUTHORIZED_MESSAGE;
            case FORBIDDEN -> GENERIC_UNAUTHORIZED_MESSAGE;
            case CONFLICT -> GENERIC_CONFLICT_MESSAGE;
            case UNPROCESSABLE_ENTITY -> GENERIC_VALIDATION_MESSAGE;
            default -> GENERIC_ERROR_MESSAGE;
        };
        return createGenericErrorResponse(status, message);
    }

    /**
     * Logs the actual error for debugging while returning a generic response
     */
    public static ResponseEntity<Response> logAndCreateGenericError(Exception ex, HttpStatus status, String userMessage) {
        log.error("Internal error occurred: {}", ex.getMessage(), ex);
        return createGenericErrorResponse(status, userMessage);
    }

    /**
     * Logs the actual error for debugging while returning a generic response with default message
     */
    public static ResponseEntity<Response> logAndCreateGenericError(Exception ex, HttpStatus status) {
        log.error("Internal error occurred: {}", ex.getMessage(), ex);
        return createGenericErrorResponse(status);
    }
}
