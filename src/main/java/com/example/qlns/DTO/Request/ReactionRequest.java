package com.example.qlns.DTO.Request;

// [Chat] Reaction - Request thả cảm xúc lên tin nhắn
public class ReactionRequest {

    // [Chat] ID người thả cảm xúc
    private Long userId;

    // [Chat] ID tin nhắn cần react
    private Long messageId;

    // [Chat] Mã emoji
    private String emoji;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
