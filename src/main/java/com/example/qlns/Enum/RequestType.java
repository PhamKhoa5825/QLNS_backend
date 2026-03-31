package com.example.qlns.Enum;

public enum RequestType {
    LEAVE_ANNUAL,   // Nghỉ phép năm (Có lương, trừ quỹ phép)
    LEAVE_UNPAID,   // Nghỉ không lương (Bị trừ lương)
    SICK_LEAVE,     // Nghỉ ốm (BHXH trả)
    OVERTIME,       // Làm thêm giờ
    BUSINESS_TRIP,  // Công tác
    PUNCH_CORRECTION,// Bổ sung công (Quyên chấm công)
    RESIGNATION     // Xin thôi việc
}
