package com.tiketkonser.ticket_service.handler;

import com.tiketkonser.ticket_service.payload.res.ErrorResponse;
import com.tiketkonser.ticket_service.payload.res.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder().timestamp(LocalDateTime.now()).status(404).error("NOT_FOUND").message(ex.getMessage()).path(req.getRequestURI()).build()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder().timestamp(LocalDateTime.now()).status(400).error("BAD_REQUEST").message(ex.getMessage()).path(req.getRequestURI()).build()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder().timestamp(LocalDateTime.now()).status(409).error("CONFLICT").message(ex.getMessage()).path(req.getRequestURI()).build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder().timestamp(LocalDateTime.now()).status(403).error("FORBIDDEN").message(ex.getMessage()).path(req.getRequestURI()).build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            String msg = err.getDefaultMessage();
            errors.put(field, msg);
        });

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ValidationErrorResponse.builder().timestamp(LocalDateTime.now()).status(422).error("VALIDATION_ERROR").message("Validasi input gagal").path(req.getRequestURI()).errors(errors).build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder().timestamp(LocalDateTime.now()).status(500).error("INTERNAL_SERVER_ERROR").message(ex.getMessage()).path(req.getRequestURI()).build()
        );
    }
}