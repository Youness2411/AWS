package com.example.OnePieceTheoryEvaluator.exceptions;

/**
 * Custom exception for data access errors that should be masked from the frontend
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
