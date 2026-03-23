package com.example.qlns.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// [Chat] Reaction - Thả cảm xúc lên tin nhắn
@Entity
@Table(name = "message_reactions", uniqueConstraints = @UniqueConstraint(columnNames = { "message_id", "user_id",
        "emoji" }))
public class MessageReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Chat] Tin nhắn được react
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // [Chat] Người thả reaction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // [Chat] Mã emoji reaction
    @Column(nullable = false, length = 10)
    private String emoji;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public MessageReaction() {
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() {
        return id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
