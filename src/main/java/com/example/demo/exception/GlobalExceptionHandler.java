package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import com.example.demo.error.ApiErrorCodes;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static String path(WebRequest request) {
    return request.getDescription(false).replace("uri=", "");
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex, WebRequest request) {
    logger.warn("Resource not found: {}", ex.getMessage());
    ErrorResponse body =
        new ErrorResponse(
            HttpStatus.NOT_FOUND.value(), ex.getCode(), ex.getMessage(), path(request));
    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(
      BadRequestException ex, WebRequest request) {
    logger.warn("Bad request: {}", ex.getMessage());
    ErrorResponse body =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(), ex.getCode(), ex.getMessage(), path(request));
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(
      UnauthorizedException ex, WebRequest request) {
    logger.warn("Unauthorized: {}", ex.getMessage());
    ErrorResponse body =
        new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(), ex.getCode(), ex.getMessage(), path(request));
    return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, WebRequest request) {
    logger.warn("Validation error: {}", ex.getMessage());
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      if (error instanceof FieldError fe) {
        fieldErrors.put(fe.getField(), fe.getDefaultMessage());
      } else {
        fieldErrors.put(error.getObjectName(), error.getDefaultMessage());
      }
    });
    ErrorResponse body =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ApiErrorCodes.VALIDATION_FAILED,
            "Validation failed",
            path(request),
            fieldErrors);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    logger.warn("Constraint violation: {}", ex.getMessage());
    ErrorResponse body =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ApiErrorCodes.CONSTRAINT_VIOLATION,
            ex.getMessage(),
            path(request));
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
    logger.error("Unexpected error: {}", ex.getMessage(), ex);
    ErrorResponse body =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ApiErrorCodes.INTERNAL_ERROR,
            "An unexpected error occurred",
            path(request));
    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
