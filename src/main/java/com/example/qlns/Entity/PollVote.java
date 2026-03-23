package com.example.qlns.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// [Chat] Lượt vote của từng user trong Poll
@Entity
@Table(name = "poll_votes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"poll_id", "user_id", "option_index"}))
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Chat] Poll mà user vote
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    // [Chat] Người vote
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // [Chat] Index lựa chọn (0-based, tương ứng với array options trong Poll)
    @Column(name = "option_index", nullable = false)
    private Integer optionIndex;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PollVote() {}

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }

    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getOptionIndex() { return optionIndex; }
    public void setOptionIndex(Integer optionIndex) { this.optionIndex = optionIndex; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
