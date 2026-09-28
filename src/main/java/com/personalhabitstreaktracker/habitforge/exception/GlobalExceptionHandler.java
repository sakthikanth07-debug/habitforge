package com.personalhabitstreaktracker.habitforge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HabitNotFoundException.class)
        public ResponseEntity<Map<String, String>> handleHabitNotFound(
            HabitNotFoundException exception) {

                return error(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception) {

                return error(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception) {

                return error("Invalid request. Please check the submitted values.", HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, String>> handleUnreadableRequest() {
                return error("Request body contains invalid or missing values.", HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, String>> handleDataIntegrityViolation() {
                return error("The request conflicts with existing data.", HttpStatus.CONFLICT);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, String>> handleUnexpectedError() {
                return error("The server could not complete your request. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private ResponseEntity<Map<String, String>> error(String message, HttpStatus status) {
                return ResponseEntity.status(status).body(Map.of("message", message));
    }
}