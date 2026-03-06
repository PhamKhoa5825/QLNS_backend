package com.example.qlns.Exception;

// =============================================
// 1. BASE EXCEPTION
// =============================================
public class AppException extends RuntimeException {
    private final int statusCode;

    public AppException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }
}

