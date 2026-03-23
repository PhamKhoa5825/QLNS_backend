package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Poll;

import java.util.List;
import java.util.Map;

// [Chat] Bình chọn - DTO hiển thị chi tiết poll và kết quả vote
public class PollDTO {

    private Long id;
    private Long roomId;
    private Long messageId;
    private Long creatorId;
    private String creatorName;
    private String question;

    // [Chat] Danh sách lựa chọn
    private List<String> options;

    // [Chat] Số vote cho từng option (key = optionIndex, value = count)
    private Map<Integer, Long> voteCounts;

    // [Chat] Tổng số người đã vote
    private int totalVoters;

    // [Chat] Các option mà user hiện tại đã chọn
    private List<Integer> myVotes;

    private Boolean isClosed;
    private String deadline;
    private String createdAt;

    // [Chat] Factory method từ Poll entity
    public static PollDTO from(Poll poll) {
        PollDTO dto = new PollDTO();
        dto.id = poll.getId();
        dto.messageId = poll.getMessageId();
        dto.question = poll.getQuestion();
        dto.isClosed = poll.getIsClosed();
        dto.deadline = poll.getDeadline() != null ? poll.getDeadline().toString() : null;
        dto.createdAt = poll.getCreatedAt() != null ? poll.getCreatedAt().toString() : null;
        if (poll.getRoom() != null) dto.roomId = poll.getRoom().getId();
        if (poll.getCreator() != null) {
            dto.creatorId = poll.getCreator().getId();
            dto.creatorName = poll.getCreator().getUsername();
        }
        return dto;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public Long getMessageId() { return messageId; }
    public Long getCreatorId() { return creatorId; }
    public String getCreatorName() { return creatorName; }
    public String getQuestion() { return question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Map<Integer, Long> getVoteCounts() { return voteCounts; }
    public void setVoteCounts(Map<Integer, Long> voteCounts) { this.voteCounts = voteCounts; }

    public int getTotalVoters() { return totalVoters; }
    public void setTotalVoters(int totalVoters) { this.totalVoters = totalVoters; }

    public List<Integer> getMyVotes() { return myVotes; }
    public void setMyVotes(List<Integer> myVotes) { this.myVotes = myVotes; }

    public Boolean getIsClosed() { return isClosed; }
    public String getDeadline() { return deadline; }
    public String getCreatedAt() { return createdAt; }
}
