package com.example.qlns.DTO.Request;

import java.util.List;

// [Chat] Request thêm thành viên vào nhóm chat
public class AddMembersRequest {

    private List<Long> memberUserIds;    // danh sách userId cần thêm

    public List<Long> getMemberUserIds() { return memberUserIds; }
    public void setMemberUserIds(List<Long> memberUserIds) { this.memberUserIds = memberUserIds; }
}
