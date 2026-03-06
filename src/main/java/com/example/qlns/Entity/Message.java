package com.example.qlns.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Employee sender;            // ← Employee gửi

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Employee receiver;          // ← Employee nhận (null nếu gửi phòng ban)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.PRIVATE;

    private boolean isRead = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum MessageType { PRIVATE, DEPARTMENT, BROADCAST }

    public Message() {}

    public Message(Employee sender, Employee receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = MessageType.PRIVATE;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Employee getSender() { return sender; }
    public void setSender(Employee sender) { this.sender = sender; }
    public Employee getReceiver() { return receiver; }
    public void setReceiver(Employee receiver) { this.receiver = receiver; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
