package com.example.qlns.Exception;

// =============================================
// 4. 409 - TRÙNG LẶP
// =============================================
public class DuplicateException extends AppException {
    public DuplicateException(String field, String value) {
        super(field + " '" + value + "' đã tồn tại", 409);
    }
}
