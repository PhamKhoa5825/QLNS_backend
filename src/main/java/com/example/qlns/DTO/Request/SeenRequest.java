package com.example.qlns.DTO.Request;

// [Chat] Đã xem - Request đánh dấu đã đọc tin nhắn trong phòng
public class SeenRequest {

    // [Chat] ID người dùng đang đánh dấu đã xem
    private Long userId;

    // [Chat] Phòng chat
    private Long roomId;

    // [Chat] ID tin nhắn cuối cùng đã đọc
    private Long lastSeenMessageId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getLastSeenMessageId() { return lastSeenMessageId; }
    public void setLastSeenMessageId(Long lastSeenMessageId) { this.lastSeenMessageId = lastSeenMessageId; }
}
