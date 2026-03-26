package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.ChatRoom;

import java.time.format.DateTimeFormatter;

// [Chat] DTO phòng chat - bao gồm avatarUrl, unreadCount, description, createdBy cho Frontend
public class ChatRoomDTO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Long id;
    private String name;
    private String type;
    private Long departmentId;
    private Long createdBy;
    private String createdByName;
    private String lastMessage; // preview tin nhắn mới nhất
    private String lastMessageTime;
    private long unreadCount; // badge số tin chưa đọc
    private String otherParticipantName; // dùng cho phòng 1-1
    private Long otherParticipantId;
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
        dto.createdAt = cr.getCreatedAt() != null ? cr.getCreatedAt().format(FMT) : null;
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

    public String getOtherParticipantName() {
        return otherParticipantName;
    }

    public void setOtherParticipantName(String n) {
        this.otherParticipantName = n;
    }

    public Long getOtherParticipantId() {
        return otherParticipantId;
    }

    public void setOtherParticipantId(Long id) {
        this.otherParticipantId = id;
    }

    public void setCreatedByName(String n) {
        this.createdByName = n;
    }
}
