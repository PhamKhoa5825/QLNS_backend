package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.ChatRoom;

// [Chat] DTO phòng chat - bao gồm avatarUrl, unreadCount, description, createdBy cho Frontend
public class ChatRoomDTO {
    private Long id;
    private String name;
    private String type;
    private Long departmentId;

    // [Chat] Người tạo phòng chat
    private Long createdBy;
    private String createdByName;

    // [Chat] Tin nhắn mới nhất (hiển thị preview ở danh sách phòng)
    private String lastMessage;
    private String lastMessageTime;

    // [Chat] Badge số tin chưa đọc
    private long unreadCount;

    // [Chat] Thời gian tạo phòng
    private String createdAt;

    public static ChatRoomDTO from(ChatRoom cr) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.id = cr.getId();
        dto.name = cr.getName();
        dto.type = cr.getType().name();
        if (cr.getCreatedBy() != null) {
            dto.createdBy = cr.getCreatedBy().getId();
            dto.createdByName = cr.getCreatedBy().getUsername();
        }
        dto.createdAt = cr.getCreatedAt() != null ? cr.getCreatedAt().toString() : null;
        if (cr.getDepartment() != null)
            dto.departmentId = cr.getDepartment().getId();
        return dto;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String t) {
        this.lastMessageTime = t;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
