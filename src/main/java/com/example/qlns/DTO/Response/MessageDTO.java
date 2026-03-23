package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Message;

import java.util.List;

// [Chat] DTO tin nhắn - mở rộng với file, edit, recall, pin, reply, voice, reactions, seen
public class MessageDTO {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String message;
    private String messageType;
    private String status;
    private String createdAt;

    // [Chat] Đa phương tiện
    private String fileUrl;
    private String fileName;
    private Long fileSize;

    // [Chat] Reply/Trích dẫn
    private Long replyToId;
    private MessageDTO replyToMessage;

    // [Chat] Thu hồi
    private Boolean isRecalled;

    // [Chat] Ghim
    private Boolean isPinned;
    private Long pinnedBy;
    private String pinnedAt;

    // [Chat] Voice
    private Integer voiceDuration;

    // [Chat] Metadata bổ sung (task card, poll, bot data)
    private String metadata;

    // [Chat] Reactions tổng hợp theo emoji
    private List<ReactionDTO> reactions;

    // [Chat] Danh sách người đã xem
    private List<ReadReceiptDTO> seenBy;

    // [Chat] Factory method từ Message entity
    public static MessageDTO from(Message m) {
        MessageDTO dto = new MessageDTO();
        dto.id = m.getId();
        dto.message = m.getMessage();
        dto.messageType = m.getMessageType().name();
        dto.status = m.getStatus() != null ? m.getStatus().name() : null;
        dto.createdAt = m.getCreatedAt() != null ? m.getCreatedAt().toString() : null;
        dto.fileUrl = m.getFileUrl();
        dto.fileName = m.getFileName();
        dto.fileSize = m.getFileSize();
        dto.replyToId = m.getReplyToId();
        dto.isRecalled = m.getIsRecalled();
        dto.isPinned = m.getIsPinned();
        dto.pinnedBy = m.getPinnedBy();
        dto.pinnedAt = m.getPinnedAt() != null ? m.getPinnedAt().toString() : null;
        dto.voiceDuration = m.getVoiceDuration();
        dto.metadata = m.getMetadata();
        if (m.getRoom() != null) dto.roomId = m.getRoom().getId();
        if (m.getSender() != null) {
            dto.senderId = m.getSender().getId();
            dto.senderName = m.getSender().getUsername();
        }
        return dto;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public Long getSenderId() { return senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public String getMessage() { return message; }
    public String getMessageType() { return messageType; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getFileUrl() { return fileUrl; }
    public String getFileName() { return fileName; }
    public Long getFileSize() { return fileSize; }
    public Long getReplyToId() { return replyToId; }

    public MessageDTO getReplyToMessage() { return replyToMessage; }
    public void setReplyToMessage(MessageDTO replyToMessage) { this.replyToMessage = replyToMessage; }

    public Boolean getIsRecalled() { return isRecalled; }
    public Boolean getIsPinned() { return isPinned; }
    public Long getPinnedBy() { return pinnedBy; }
    public String getPinnedAt() { return pinnedAt; }
    public Integer getVoiceDuration() { return voiceDuration; }
    public String getMetadata() { return metadata; }

    public List<ReactionDTO> getReactions() { return reactions; }
    public void setReactions(List<ReactionDTO> reactions) { this.reactions = reactions; }

    public List<ReadReceiptDTO> getSeenBy() { return seenBy; }
    public void setSeenBy(List<ReadReceiptDTO> seenBy) { this.seenBy = seenBy; }
}
