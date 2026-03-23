package com.example.qlns.Entity;

import com.example.qlns.Enum.MessageStatus;
import com.example.qlns.Enum.MessageType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    // [Chat] Trạng thái gửi/nhận/đọc của tin nhắn
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status = MessageStatus.SENT;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private Boolean isRead = false;

    // [Chat] Đa phương tiện - URL file đã upload (ảnh, file, voice)
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    // [Chat] Đa phương tiện - Tên file gốc
    @Column(name = "file_name", length = 255)
    private String fileName;

    // [Chat] Đa phương tiện - Kích thước file (bytes)
    @Column(name = "file_size")
    private Long fileSize;

    // [Chat] Reply/Trích dẫn - ID tin nhắn đang trả lời
    @Column(name = "reply_to_id")
    private Long replyToId;

    // [Chat] Chỉnh sửa - Tin nhắn đã bị sửa hay chưa
    @Column(name = "is_edited")
    private Boolean isEdited = false;

    // [Chat] Chỉnh sửa - Thời gian sửa lần cuối
    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    // [Chat] Thu hồi - Tin nhắn đã bị thu hồi hay chưa
    @Column(name = "is_recalled")
    private Boolean isRecalled = false;

    // [Chat] Thu hồi - Thời gian thu hồi
    @Column(name = "recalled_at")
    private LocalDateTime recalledAt;

    // [Chat] Ghim - Tin nhắn đang ghim hay không
    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    // [Chat] Ghim - Người ghim tin nhắn
    @Column(name = "pinned_by")
    private Long pinnedBy;

    // [Chat] Ghim - Thời gian ghim
    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    // [Chat] Voice - Thời lượng tin nhắn thoại (giây)
    @Column(name = "voice_duration")
    private Integer voiceDuration;

    // [Chat] Metadata bổ sung dạng JSON (task card data, poll data, bot data)
    @Column(columnDefinition = "TEXT")
    private String metadata;

    public Message() {
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }

    public ChatRoom getRoom() { return room; }
    public void setRoom(ChatRoom room) { this.room = room; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }


    public Boolean getIsRecalled() { return isRecalled; }
    public void setIsRecalled(Boolean isRecalled) { this.isRecalled = isRecalled; }

    public LocalDateTime getRecalledAt() { return recalledAt; }
    public void setRecalledAt(LocalDateTime recalledAt) { this.recalledAt = recalledAt; }

    public Boolean getIsPinned() { return isPinned; }
    public void setIsPinned(Boolean isPinned) { this.isPinned = isPinned; }

    public Long getPinnedBy() { return pinnedBy; }
    public void setPinnedBy(Long pinnedBy) { this.pinnedBy = pinnedBy; }

    public LocalDateTime getPinnedAt() { return pinnedAt; }
    public void setPinnedAt(LocalDateTime pinnedAt) { this.pinnedAt = pinnedAt; }

    public Integer getVoiceDuration() { return voiceDuration; }
    public void setVoiceDuration(Integer voiceDuration) { this.voiceDuration = voiceDuration; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}