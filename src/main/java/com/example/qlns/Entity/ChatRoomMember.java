package com.example.qlns.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_members")
public class ChatRoomMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // [Chat] Vai trò trong phòng chat (ADMIN = quản trị, MEMBER = thành viên)
    @Column(length = 20)
    private String role = "MEMBER";

    // [Chat] ID tin nhắn cuối cùng đã đọc - dùng tính số tin chưa đọc
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;


    @CreationTimestamp
    private LocalDateTime joinedAt;

    public ChatRoomMember() {}

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }

    public ChatRoom getRoom() { return room; }
    public void setRoom(ChatRoom room) { this.room = room; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }


    public LocalDateTime getJoinedAt() { return joinedAt; }
}
