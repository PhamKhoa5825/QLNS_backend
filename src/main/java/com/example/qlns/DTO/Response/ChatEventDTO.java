package com.example.qlns.DTO.Response;

// [Chat] WebSocket event DTO - Gói tin gửi qua WebSocket cho client
// Mỗi event có type để client biết cách xử lý (tin nhắn mới, sửa, xóa, reaction...)
public class ChatEventDTO {

    // [Chat] Loại sự kiện (NEW_MESSAGE, EDIT, RECALL, PIN, REACTION, TYPING, SEEN...)
    private String eventType;

    // [Chat] ID phòng chat liên quan
    private Long roomId;

    // [Chat] Tin nhắn liên quan (null nếu event không liên quan đến message)
    private MessageDTO message;

    // [Chat] Dữ liệu bổ sung dạng Object (tùy theo eventType)
    private Object data;

    // [Chat] ID user gây ra event
    private Long triggeredBy;

    public ChatEventDTO() {}

    // [Chat] Factory method tạo event nhanh
    public static ChatEventDTO of(String eventType, Long roomId, MessageDTO message, Object data, Long triggeredBy) {
        ChatEventDTO dto = new ChatEventDTO();
        dto.eventType = eventType;
        dto.roomId = roomId;
        dto.message = message;
        dto.data = data;
        dto.triggeredBy = triggeredBy;
        return dto;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public MessageDTO getMessage() { return message; }
    public void setMessage(MessageDTO message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public Long getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(Long triggeredBy) { this.triggeredBy = triggeredBy; }
}
