package com.example.qlns.Exception;

// =============================================
// 3. 400 - DỮ LIỆU KHÔNG HỢP LỆ
// =============================================
public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(message, 400);
    }
}
