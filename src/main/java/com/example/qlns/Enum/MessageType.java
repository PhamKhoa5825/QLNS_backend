package com.example.qlns.Enum;

// [Chat] Loại tin nhắn - mở rộng thêm VOICE, TASK_CARD, POLL, BOT, SYSTEM
public enum MessageType {
    TEXT, // Tin nhắn văn bản
    IMAGE, // Ảnh (selfie điểm danh, ảnh chụp)
    FILE, // File đính kèm (PDF, Excel, Word)
    VOICE, // Tin nhắn thoại
    TASK_CARD, // Thẻ công việc hiển thị trong chat
    POLL, // Bình chọn
    BOT, // Tin nhắn từ bot
    SYSTEM // Tin nhắn hệ thống
}