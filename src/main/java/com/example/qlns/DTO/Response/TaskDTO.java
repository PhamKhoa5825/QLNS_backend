package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Task;

public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String deadline;
    private String completedAt;
    private String attachmentUrl;
    private Long assignedById;
    private String assignedByName;
    private Long assignedToId;
    private String assignedToName;
    private String createdAt;

    public static TaskDTO from(Task t) {
        TaskDTO dto = new TaskDTO();
        dto.id = t.getId();
        dto.title = t.getTitle();
        dto.description = t.getDescription();
        dto.priority = t.getPriority().name();
        dto.status = t.getStatus().name();
        dto.deadline = t.getDeadline() != null ? t.getDeadline().toString() : null;
        dto.completedAt = t.getCompletedAt() != null ? t.getCompletedAt().toString() : null;
        dto.attachmentUrl = t.getAttachmentUrl();
        dto.createdAt = t.getCreatedAt() != null ? t.getCreatedAt().toString() : null;
        if (t.getAssignedBy() != null) {
            dto.assignedById = t.getAssignedBy().getId();
            dto.assignedByName = t.getAssignedBy().getFullName();
        }
        if (t.getAssignedTo() != null) {
            dto.assignedToId = t.getAssignedTo().getId();
            dto.assignedToName = t.getAssignedTo().getFullName();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public Long getAssignedById() {
        return assignedById;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
