package com.example.qlns.DTO;

import com.example.qlns.Entity.Task;

// =============================================
// 4. TASK DTO
// TV3 dùng
// =============================================
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String deadline;
    private Long assigneeId;
    private String assigneeName;
    private Long createdById;
    private String createdByName;
    private Long departmentId;
    private String departmentName;
    private String createdAt;

    public static TaskDTO from(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.id = task.getId();
        dto.title = task.getTitle();
        dto.description = task.getDescription();
        dto.priority = task.getPriority().name();
        dto.status = task.getStatus().name();
        dto.deadline = task.getDeadline() != null ? task.getDeadline().toString() : null;
        dto.createdAt = task.getCreatedAt() != null ? task.getCreatedAt().toString() : null;

        if (task.getAssignee() != null) {
            dto.assigneeId = task.getAssignee().getId();
            dto.assigneeName = task.getAssignee().getFullName();
        }
        if (task.getCreatedBy() != null) {
            dto.createdById = task.getCreatedBy().getId();
            dto.createdByName = task.getCreatedBy().getFullName();
        }
        if (task.getDepartment() != null) {
            dto.departmentId = task.getDepartment().getId();
            dto.departmentName = task.getDepartment().getName();
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

    public Long getAssigneeId() {
        return assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
