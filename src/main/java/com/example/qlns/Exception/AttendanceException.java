package com.example.qlns.Exception;

// =============================================
// 7. CHẤM CÔNG (TV3 dùng)
// =============================================
public class AttendanceException extends AppException {
    public AttendanceException(String message) {
        super(message, 400);
    }
}
