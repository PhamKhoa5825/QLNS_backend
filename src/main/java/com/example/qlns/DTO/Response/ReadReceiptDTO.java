package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.MessageReadReceipt;

import java.time.format.DateTimeFormatter;

// [Chat] Đã xem - DTO hiển thị ai đã xem tin nhắn và lúc nào
public class ReadReceiptDTO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long userId;
    private String userName;
    private String fullName; // set từ Service vì entity không lưu trực tiếp
    private String seenAt;

    public static ReadReceiptDTO from(MessageReadReceipt receipt) {
        ReadReceiptDTO dto = new ReadReceiptDTO();
        dto.userId = receipt.getUser().getId();
        dto.userName = receipt.getUser().getUsername();
        dto.seenAt = receipt.getSeenAt() != null ? receipt.getSeenAt().format(FMT) : null;
        return dto;
    }

    // ── Getters ──────────────────────────────────────

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String n) {
        this.fullName = n;
    }

    public String getSeenAt() {
        return seenAt;
    }
}
