package com.example.qlns.DTO.Request;

// [Chat] Request đổi tên phòng chat nhóm
public class UpdateRoomNameRequest {

    private String name;
    private Long updatedByUserId;   // người đổi tên (để ghi tin nhắn hệ thống)

    // ── Getters & Setters ──────────────────────────────────────

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getUpdatedByUserId() { return updatedByUserId; }
    public void setUpdatedByUserId(Long updatedByUserId) { this.updatedByUserId = updatedByUserId; }
}
