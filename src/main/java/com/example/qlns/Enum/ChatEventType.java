package com.example.qlns.Enum;

// [Chat] Loại sự kiện WebSocket broadcast tới client
public enum ChatEventType {
    NEW_MESSAGE,    // Tin nhắn mới
    RECALL,         // Thu hồi tin nhắn
    PIN,            // Ghim tin nhắn
    UNPIN,          // Bỏ ghim tin nhắn
    REACTION,       // Thả cảm xúc
    TYPING,         // Đang gõ
    SEEN,           // Đã xem
    DELIVERED,      // Đã nhận
    MEMBER_JOIN,    // Thành viên mới vào nhóm
    MEMBER_LEAVE,   // Thành viên rời nhóm
    POLL_CREATED,   // Tạo bình chọn mới
    POLL_VOTE       // Có người vote
}
