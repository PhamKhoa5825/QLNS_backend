package com.example.qlns.Exception;

// =============================================
// 8. GPS (TV3 dùng)
// =============================================
public class LocationException extends AppException {
    public LocationException(double distance) {
        super("Bạn đang cách công ty " + (int) distance + "m. Cần trong vòng 1km để chấm công", 400);
    }
}
