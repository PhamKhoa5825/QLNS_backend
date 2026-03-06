package com.example.qlns.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Bắt tất cả exception và trả về JSON đẹp cho Android
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt tất cả AppException (và các class con)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        return buildResponse(ex.getMessage(), ex.getStatusCode());
    }

    // Bắt lỗi không mong muốn
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse("Lỗi hệ thống: " + ex.getMessage(), 500);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String message, int status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}