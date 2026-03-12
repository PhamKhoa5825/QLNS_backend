package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Notification;

public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private String targetType;
    private Long departmentId;
    private String departmentName;
    private String createdByName;
    private String createdAt;
    private boolean isRead;         // Từ UserNotification

    public static NotificationDTO from(Notification n, boolean isRead) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = n.getId();
        dto.title = n.getTitle();
        dto.content = n.getContent();
        dto.targetType = n.getTargetType() != null ? n.getTargetType().name() : "COMPANY";
        dto.createdAt = n.getCreatedAt() != null ? n.getCreatedAt().toString() : null;
        dto.isRead = isRead;
        if (n.getDepartment() != null) {
            dto.departmentId = n.getDepartment().getId();
            dto.departmentName = n.getDepartment().getName();
        }
        if (n.getCreatedBy() != null) {
            dto.createdByName = n.getCreatedBy().getFullName();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }
}
