package com.example.qlns.Enum;

public enum TaskStatus {
    PENDING,    // Chưa nhận
    ACCEPTED,       // Đã nhận
    UNDER_REVIEW,   // Chờ duyệt
    DONE,           // Hoàn thành
    REJECTED,       // Từ chối/Yêu cầu làm lại
    OVERDUE         // Quá hạn (auto update mỗi sáng)
}
