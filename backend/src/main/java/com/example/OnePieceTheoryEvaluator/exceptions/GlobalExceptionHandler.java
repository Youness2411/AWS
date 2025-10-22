package com.example.OnePieceTheoryEvaluator.exceptions;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.util.ErrorResponseUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle all unexpected exceptions with generic error message
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> handleAllExceptions(Exception ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle data access exceptions (JPA, database errors)
    @ExceptionHandler({DataAccessException.class, SQLException.class, EntityNotFoundException.class})
    public ResponseEntity<Response> handleDataAccessExceptions(Exception ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.INTERNAL_SERVER_ERROR, 
            ErrorResponseUtil.GENERIC_ERROR_MESSAGE);
    }

    // Handle our custom data access exceptions
    @ExceptionHandler(com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException.class)
    public ResponseEntity<Response> handleCustomDataAccessException(com.example.OnePieceTheoryEvaluator.exceptions.DataAccessException ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle validation exceptions (these can be shown to user)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Response> handleValidationException(ValidationException ex){
        return ErrorResponseUtil.createGenericErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Handle constraint violations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response> handleConstraintViolationException(ConstraintViolationException ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.BAD_REQUEST, 
            ErrorResponseUtil.GENERIC_VALIDATION_MESSAGE);
    }

    // Handle method argument validation
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Response> handleValidationExceptions(Exception ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.BAD_REQUEST, 
            ErrorResponseUtil.GENERIC_VALIDATION_MESSAGE);
    }

    // Handle malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.BAD_REQUEST, 
            ErrorResponseUtil.GENERIC_BAD_REQUEST_MESSAGE);
    }

    // Handle missing request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.BAD_REQUEST, 
            ErrorResponseUtil.GENERIC_BAD_REQUEST_MESSAGE);
    }

    // Handle type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.BAD_REQUEST, 
            ErrorResponseUtil.GENERIC_BAD_REQUEST_MESSAGE);
    }

    // Handle unsupported HTTP methods
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.METHOD_NOT_ALLOWED, 
            ErrorResponseUtil.GENERIC_BAD_REQUEST_MESSAGE);
    }

    // Handle file upload size exceeded
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Response> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex){
        return ErrorResponseUtil.logAndCreateGenericError(ex, HttpStatus.PAYLOAD_TOO_LARGE, 
            "File size too large. Please choose a smaller file.");
    }

    // Handle custom business logic exceptions (these can be shown to user)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response> handleNotFoundException(NotFoundException ex){
        return ErrorResponseUtil.createGenericErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NameValueRequiredException.class)
    public ResponseEntity<Response> handleNameValueRequiredException(NameValueRequiredException ex){
        return ErrorResponseUtil.createGenericErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Response> handleInvalidCredentialsException(InvalidCredentialsException ex){
        return ErrorResponseUtil.createGenericErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }




}