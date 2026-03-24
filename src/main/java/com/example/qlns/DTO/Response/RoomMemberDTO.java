package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.ChatRoomMember;

// [Chat] DTO thông tin thành viên trong phòng chat
public class RoomMemberDTO {
    private Long userId;
    private String username;
    private String role;
    private String joinedAt;

    public static RoomMemberDTO from(ChatRoomMember m) {
        RoomMemberDTO dto = new RoomMemberDTO();
        dto.userId = m.getUser().getId();
        dto.username = m.getUser().getUsername();
        dto.role = m.getRole();
        dto.joinedAt = m.getJoinedAt() != null ? m.getJoinedAt().toString() : null;
        return dto;
    }

    // ── Getters ──────────────────────────────────────

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getJoinedAt() { return joinedAt; }
}
