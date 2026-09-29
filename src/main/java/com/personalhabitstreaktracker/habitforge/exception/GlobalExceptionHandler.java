package com.personalhabitstreaktracker.habitforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HabitNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleHabitNotFound(
            HabitNotFoundException exception) {

                return error(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {

                return error(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

                return error("Invalid request. Please check the submitted values.", HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleUnreadableRequest() {
                return error("Request body contains invalid or missing values.", HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation() {
                return error("The request conflicts with existing data.", HttpStatus.CONFLICT);
        }

            @ExceptionHandler(AuthenticationException.class)
            public ResponseEntity<Map<String, Object>> handleAuthenticationFailure() {
                return error("Email or password is incorrect.", HttpStatus.UNAUTHORIZED);
            }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleUnexpectedError() {
                return error("The server could not complete your request. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private ResponseEntity<Map<String, Object>> error(String message, HttpStatus status) {
                return ResponseEntity.status(status).body(Map.of("message", message, "status", status.value()));
    }
}