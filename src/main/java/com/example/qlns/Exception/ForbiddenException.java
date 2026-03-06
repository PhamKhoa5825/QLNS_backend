package com.example.qlns.Exception;

// =============================================
// 5. 403 - KHÔNG CÓ QUYỀN
// =============================================
public class ForbiddenException extends AppException {
    public ForbiddenException() {
        super("Bạn không có quyền thực hiện hành động này", 403);
    }

    public ForbiddenException(String message) {
        super(message, 403);
    }
}
