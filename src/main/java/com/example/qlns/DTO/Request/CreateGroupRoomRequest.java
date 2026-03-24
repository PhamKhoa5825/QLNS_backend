package com.example.qlns.DTO.Request;

import java.util.List;

// [Chat] Request tạo phòng chat nhóm (dành cho Manager)
public class CreateGroupRoomRequest {

    private String name;
    private Long createdByUserId;
    private Long departmentId;              // tuỳ chọn - gắn theo phòng ban
    private List<Long> memberUserIds;       // danh sách userId thành viên ban đầu

    // ── Getters & Setters ──────────────────────────────────────

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public List<Long> getMemberUserIds() { return memberUserIds; }
    public void setMemberUserIds(List<Long> memberUserIds) { this.memberUserIds = memberUserIds; }
}
