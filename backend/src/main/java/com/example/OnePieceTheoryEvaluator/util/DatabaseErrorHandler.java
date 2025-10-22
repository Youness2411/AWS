package com.example.OnePieceTheoryEvaluator.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.sql.SQLException;

@Slf4j
public class DatabaseErrorHandler {

    /**
     * Wraps database operations and converts internal errors to generic ones
     */
    public static <T> T executeWithErrorHandling(DatabaseOperation<T> operation) {
        try {
            return operation.execute();
        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation: {}", ex.getMessage(), ex);
            throw new com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException("Data integrity constraint violated");
        } catch (EmptyResultDataAccessException ex) {
            log.error("No data found: {}", ex.getMessage(), ex);
            throw new com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException("No data found");
        } catch (InvalidDataAccessApiUsageException ex) {
            log.error("Invalid data access usage: {}", ex.getMessage(), ex);
            throw new com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException("Invalid database operation");
        } catch (DataAccessException ex) {
            log.error("Database access error: {}", ex.getMessage(), ex);
            throw new com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException("Database operation failed");
        } catch (SQLException ex) {
            log.error("SQL error: {}", ex.getMessage(), ex);
            throw new com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException("Database operation failed");
        } catch (Exception ex) {
            log.error("Unexpected database error: {}", ex.getMessage(), ex);
            throw new com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException("Database operation failed");
        }
    }

    /**
     * Functional interface for database operations
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute() throws Exception;
    }
}
