package com.example.qlns.DTO;

import com.example.qlns.Entity.Notification;

// =============================================
// 6. NOTIFICATION DTO
// TV4 dùng
// =============================================
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Long employeeId;
    private Long referenceId;
    private boolean isRead;
    private String createdAt;

    public static NotificationDTO from(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = n.getId();
        dto.title = n.getTitle();
        dto.content = n.getContent();
        dto.type = n.getType().name();
        dto.referenceId = n.getReferenceId();
        dto.isRead = n.isRead();
        dto.createdAt = n.getCreatedAt() != null ? n.getCreatedAt().toString() : null;

        if (n.getEmployee() != null) {
            dto.employeeId = n.getEmployee().getId();
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

    public String getType() {
        return type;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
