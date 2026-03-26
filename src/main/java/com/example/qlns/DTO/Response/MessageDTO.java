package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Message;
import java.time.format.DateTimeFormatter;
import java.util.List;

// [Chat] DTO tin nhắn - bao gồm file, thu hồi, reply, voice, seen
public class MessageDTO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String message;
    private String messageType;
    private String createdAt;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Long replyToId;
    private MessageDTO replyToMessage;
    private Boolean isRecalled;
    private String metadata;
    private List<ReadReceiptDTO> seenBy;

    public static MessageDTO from(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.id = m.getId();
        dto.message = m.getMessage();
        dto.messageType = m.getMessageType().name();
        dto.createdAt = m.getCreatedAt() != null ? m.getCreatedAt().format(FMT) : null;
        dto.fileUrl = m.getFileUrl();
        dto.fileName = m.getFileName();
        dto.fileSize = m.getFileSize();
        dto.replyToId = m.getReplyToId();
        dto.isRecalled = m.getIsRecalled();
        dto.metadata = m.getMetadata();
        if (m.getRoom() != null)
            dto.roomId = m.getRoom().getId();
        if (m.getSender() != null) {
            dto.senderId = m.getSender().getId();
            dto.senderName = m.getSender().getUsername();
        }
        return dto;
    }

    // ── Getters & Setters ──────────────────────────────────────

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

    public String getMessage() {
        return message;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Long getReplyToId() {
        return replyToId;
    }

    public MessageDTO getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(MessageDTO replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    public Boolean getIsRecalled() {
        return isRecalled;
    }

    public String getMetadata() {
        return metadata;
    }

    public List<ReadReceiptDTO> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(List<ReadReceiptDTO> seenBy) {
        this.seenBy = seenBy;
    }
}
