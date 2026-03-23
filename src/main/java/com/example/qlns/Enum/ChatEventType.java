package com.example.qlns.Enum;

// [Chat] Loại sự kiện WebSocket broadcast tới client
public enum ChatEventType {
    NEW_MESSAGE, // Tin nhắn mới
    RECALL,      // Thu hồi tin nhắn
    REACTION,    // Thả cảm xúc
    SEEN         // Đã xem
}
