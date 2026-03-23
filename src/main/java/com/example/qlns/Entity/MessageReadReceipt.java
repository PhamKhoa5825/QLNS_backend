package com.example.qlns.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// [Chat] Đã xem (Seen) - Lưu chi tiết ai đã xem tin nhắn nào và lúc nào
@Entity
@Table(name = "message_read_receipts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id"}))
public class MessageReadReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Chat] Tin nhắn được đọc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // [Chat] Người đã đọc tin nhắn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // [Chat] Thời gian người dùng xem tin nhắn (VD: "Đã xem lúc 14:30")
    @Column(name = "seen_at", nullable = false)
    private LocalDateTime seenAt;

    public MessageReadReceipt() {}

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getSeenAt() { return seenAt; }
    public void setSeenAt(LocalDateTime seenAt) { this.seenAt = seenAt; }
}
