package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.ChatRoomMember;

import java.time.format.DateTimeFormatter;

// [Chat] DTO thông tin thành viên trong phòng chat
public class RoomMemberDTO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Long userId;
    private String username;
    private String fullName; // họ tên thật từ Employee
    private String position; // chức vụ từ Employee
    private String departmentName; // tên phòng ban từ Employee
    private String role;
    private String joinedAt;
    private Long lastReadMessageId; // tin nhắn cuối cùng đã đọc, dùng để tính số chưa xem

    public static RoomMemberDTO from(ChatRoomMember m) {
        RoomMemberDTO dto = new RoomMemberDTO();
        dto.userId = m.getUser().getId();
        dto.username = m.getUser().getUsername();
        dto.role = m.getRole();
        dto.joinedAt = m.getJoinedAt() != null ? m.getJoinedAt().format(FMT) : null;
        dto.lastReadMessageId = m.getLastReadMessageId();
        return dto;
    }

    // ── Getters ──────────────────────────────────────

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String n) {
        this.fullName = n;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String p) {
        this.position = p;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String d) {
        this.departmentName = d;
    }

    public String getRole() {
        return role;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }
}
