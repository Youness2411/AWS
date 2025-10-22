package com.example.OnePieceTheoryEvaluator.exceptions;

/**
 * Custom exception for validation errors that can be safely shown to the frontend
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
