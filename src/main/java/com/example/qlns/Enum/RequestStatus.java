package com.example.qlns.Enum;

public enum RequestStatus {
    PENDING,    // Chờ duyệt
    APPROVED,   // Đã duyệt
    REJECTED,   // Bị từ chối
    EXPIRED,    // Quá hạn chưa duyệt
    CANCELLED   // Người gửi tự hủy
}
