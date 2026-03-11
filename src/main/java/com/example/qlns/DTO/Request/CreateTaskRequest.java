package com.example.qlns.DTO.Request;

public class CreateTaskRequest {
    private String title;
    private String description;
    private String priority;        // LOW/MEDIUM/HIGH/URGENT
    private String deadline;        // "yyyy-MM-dd HH:mm"
    private Long assignedToId;
    private String attachmentUrl;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getDeadline() {
        return deadline;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }
}
