package com.example.qlns.DTO.Response;

// [Chat] WebSocket event DTO - Gói tin gửi qua WebSocket cho client
// Mỗi event có type để client biết cách xử lý (tin nhắn mới, sửa, xóa, reaction...)
public class ChatEventDTO {

    private String eventType; // NEW_MESSAGE, RECALL, SEEN...
    private Long roomId;
    private MessageDTO message;
    private Object data; // payload tùy theo eventType
    private Long triggeredBy; // userId gây ra event

    public ChatEventDTO() {
    }

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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public MessageDTO getMessage() {
        return message;
    }

    public void setMessage(MessageDTO message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(Long triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}
