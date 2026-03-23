package com.example.qlns.DTO.Request;

// [Chat] Request gửi tin nhắn - mở rộng thêm reply, voice, metadata
public class SendMessageRequest {
    private Long roomId;
    private Long senderId;
    private String message;
    private String messageType;     // TEXT/IMAGE/FILE/VOICE/TASK_CARD/POLL/BOT/SYSTEM

    // [Chat] Reply - ID tin nhắn đang trả lời (null = không reply)
    private Long replyToId;

    // [Chat] Voice - Thời lượng tin nhắn thoại (giây)
    private Integer voiceDuration;

    // [Chat] Metadata bổ sung dạng JSON (task card, poll, bot)
    private String metadata;

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public Integer getVoiceDuration() { return voiceDuration; }
    public void setVoiceDuration(Integer voiceDuration) { this.voiceDuration = voiceDuration; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
