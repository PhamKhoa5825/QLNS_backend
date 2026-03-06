package com.example.qlns.DTO;

import com.example.qlns.Entity.Message;

// =============================================
// 5. MESSAGE DTO
// TV4 dùng
// =============================================
public class MessageDTO {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String receiverName;
    private Long departmentId;
    private String type;
    private boolean isRead;
    private String createdAt;

    public static MessageDTO from(Message msg) {
        MessageDTO dto = new MessageDTO();
        dto.id = msg.getId();
        dto.content = msg.getContent();
        dto.type = msg.getType().name();
        dto.isRead = msg.isRead();
        dto.createdAt = msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null;

        if (msg.getSender() != null) {
            dto.senderId = msg.getSender().getId();
            dto.senderName = msg.getSender().getFullName();
            dto.senderAvatar = msg.getSender().getAvatarUrl();
        }
        if (msg.getReceiver() != null) {
            dto.receiverId = msg.getReceiver().getId();
            dto.receiverName = msg.getReceiver().getFullName();
        }
        if (msg.getDepartment() != null) {
            dto.departmentId = msg.getDepartment().getId();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
