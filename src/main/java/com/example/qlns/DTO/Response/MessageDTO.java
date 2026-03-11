package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Message;

// TV4
public class MessageDTO {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String message;
    private String messageType;
    private String createdAt;

    public static MessageDTO from(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.id = m.getId();
        dto.message = m.getMessage();
        dto.messageType = m.getMessageType().name();
        dto.createdAt = m.getCreatedAt() != null ? m.getCreatedAt().toString() : null;
        if (m.getRoom() != null) dto.roomId = m.getRoom().getId();
        if (m.getSender() != null) {
            dto.senderId = m.getSender().getId();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
