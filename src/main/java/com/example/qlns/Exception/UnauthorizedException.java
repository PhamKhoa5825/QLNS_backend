package com.example.qlns.Exception;

// =============================================
// 6. 401 - CHƯA ĐĂNG NHẬP (TV2 dùng)
// =============================================
public class UnauthorizedException extends AppException {
    public UnauthorizedException() {
        super("Vui lòng đăng nhập để tiếp tục", 401);
    }
}
