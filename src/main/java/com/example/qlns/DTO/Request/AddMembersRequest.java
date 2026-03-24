package com.example.qlns.DTO.Request;

import java.util.List;

// [Chat] Request thêm thành viên vào nhóm chat
public class AddMembersRequest {

    private Long addedByUserId;          // người thực hiện thêm (để ghi tin nhắn hệ thống)
    private List<Long> memberUserIds;    // danh sách userId cần thêm

    // ── Getters & Setters ──────────────────────────────────────

    public Long getAddedByUserId() { return addedByUserId; }
    public void setAddedByUserId(Long addedByUserId) { this.addedByUserId = addedByUserId; }

    public List<Long> getMemberUserIds() { return memberUserIds; }
    public void setMemberUserIds(List<Long> memberUserIds) { this.memberUserIds = memberUserIds; }
}
