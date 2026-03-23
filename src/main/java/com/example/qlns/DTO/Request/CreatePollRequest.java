package com.example.qlns.DTO.Request;

import java.time.LocalDateTime;
import java.util.List;

// [Chat] Bình chọn - Request tạo poll mới trong phòng chat
public class CreatePollRequest {

    // [Chat] Phòng chat chứa poll
    private Long roomId;

    // [Chat] Câu hỏi bình chọn
    private String question;

    // [Chat] Danh sách lựa chọn (VD: ["Đồng ý", "Không", "Khác"])
    private List<String> options;


    // [Chat] Hạn chót bình chọn (null = không giới hạn)
    private LocalDateTime deadline;

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }


    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
