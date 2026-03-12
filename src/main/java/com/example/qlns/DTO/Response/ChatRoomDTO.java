package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.ChatRoom;

public class ChatRoomDTO {
    private Long id;
    private String name;
    private String type;
    private Long departmentId;
    private String lastMessage;
    private String lastMessageTime;
    private java.util.List<String> memberNames;
    private String otherParticipantName;

    public java.util.List<String> getMemberNames() {
        return memberNames;
    }

    public void setMemberNames(java.util.List<String> memberNames) {
        this.memberNames = memberNames;
    }

    public String getOtherParticipantName() {
        return otherParticipantName;
    }

    public void setOtherParticipantName(String otherParticipantName) {
        this.otherParticipantName = otherParticipantName;
    }

    public static ChatRoomDTO from(ChatRoom cr) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.id = cr.getId();
        dto.name = cr.getName();
        dto.type = cr.getType().name();
        if (cr.getDepartment() != null) dto.departmentId = cr.getDepartment().getId();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String t) {
        this.lastMessageTime = t;
    }
}
