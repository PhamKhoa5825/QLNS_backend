package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.MessageReadReceipt;

// [Chat] Đã xem - DTO hiển thị ai đã xem tin nhắn và lúc nào
public class ReadReceiptDTO {

    private Long userId;
    private String userName;
    private String seenAt;      // VD: "2026-03-19T14:30:00"

    // [Chat] Factory method từ MessageReadReceipt entity
    public static ReadReceiptDTO from(MessageReadReceipt receipt) {
        ReadReceiptDTO dto = new ReadReceiptDTO();
        dto.userId = receipt.getUser().getId();
        dto.userName = receipt.getUser().getUsername();
        dto.seenAt = receipt.getSeenAt() != null ? receipt.getSeenAt().toString() : null;
        return dto;
    }

    // ── Getters ──────────────────────────────────────

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getSeenAt() { return seenAt; }
}
