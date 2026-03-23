package com.example.qlns.Enum;

// [Chat] Trạng thái tin nhắn: đang gửi, đã nhận, đã xem
public enum MessageStatus {
    SENT, // Đã gửi lên server
    DELIVERED, // Đã đến thiết bị người nhận
    READ // Người nhận đã đọc
}
