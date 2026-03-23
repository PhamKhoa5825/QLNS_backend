package com.example.qlns.DTO.Request;

// [Chat] Bình chọn - Request vote 1 option trong poll
public class PollVoteRequest {

    // [Chat] ID poll
    private Long pollId;

    // [Chat] Index lựa chọn (0-based)
    private Integer optionIndex;

    public Long getPollId() { return pollId; }
    public void setPollId(Long pollId) { this.pollId = pollId; }

    public Integer getOptionIndex() { return optionIndex; }
    public void setOptionIndex(Integer optionIndex) { this.optionIndex = optionIndex; }
}
