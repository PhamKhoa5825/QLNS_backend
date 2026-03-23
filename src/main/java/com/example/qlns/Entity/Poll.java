package com.example.qlns.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// [Chat] Bình chọn (Poll) - Tạo vote nhanh trong phòng chat
@Entity
@Table(name = "polls")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Chat] Phòng chat chứa poll
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    // [Chat] Tin nhắn liên kết (type=POLL)
    @Column(name = "message_id")
    private Long messageId;

    // [Chat] Người tạo poll
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // [Chat] Câu hỏi bình chọn
    @Column(nullable = false)
    private String question;

    // [Chat] Các lựa chọn dạng JSON array: ["Đồng ý","Không","Khác"]
    @Column(columnDefinition = "TEXT", nullable = false)
    private String options;


    // [Chat] Hạn chót bình chọn (null = không giới hạn)
    private LocalDateTime deadline;

    // [Chat] Poll đã đóng hay chưa
    @Column(name = "is_closed")
    private Boolean isClosed = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Poll() {}

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }

    public ChatRoom getRoom() { return room; }
    public void setRoom(ChatRoom room) { this.room = room; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }


    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Boolean getIsClosed() { return isClosed; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
