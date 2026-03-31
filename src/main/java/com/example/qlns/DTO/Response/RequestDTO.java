package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Request;
import com.example.qlns.Enum.RequestStatus;
import com.example.qlns.Enum.RequestType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ── Request DTO (trả về Android) ──────────────────────────────
public class RequestDTO {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String departmentName;
    private String title;
    private RequestType type;
    private List<RequestDetailDTO> details;
    private String description;
    private String fileUrl;
    private String fileName;
    private String employeeAvatarUrl; // New field
    private RequestStatus status;
    private Long reviewedById;
    private String reviewedByName;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestDTO from(Request r) {
        RequestDTO dto = new RequestDTO();
        dto.id             = r.getId();
        dto.employeeId     = r.getEmployee().getId();
        dto.employeeName   = r.getEmployee().getFullName();
        dto.departmentName = r.getEmployee().getDepartment() != null
                ? r.getEmployee().getDepartment().getName() : null;
        dto.title          = r.getTitle();
        dto.type           = r.getType();
        dto.description    = r.getDescription();
        dto.fileUrl        = r.getFileUrl();
        dto.fileName       = r.getFileName();
        dto.employeeAvatarUrl = r.getEmployee().getAvatarUrl(); // Set avatar URL from employee
        dto.status         = r.getStatus();
        if (r.getReviewedBy() != null) {
            dto.reviewedById   = r.getReviewedBy().getId();
            dto.reviewedByName = r.getReviewedBy().getFullName();
        }
        dto.rejectionReason = r.getRejectionReason();
        dto.createdAt      = r.getCreatedAt();
        dto.updatedAt      = r.getUpdatedAt();
        if (r.getDetails() != null) {
            dto.details = r.getDetails().stream()
                    .map(RequestDetailDTO::from)
                    .collect(Collectors.toList());
        }
        return dto;
    }

    // Getters
    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDepartmentName() { return departmentName; }
    public String getTitle() { return title; }
    public RequestType getType() { return type; }
    public List<RequestDetailDTO> getDetails() { return details; }
    public String getDescription() { return description; }
    public String getFileUrl() { return fileUrl; }
    public String getFileName() { return fileName; }
    public RequestStatus getStatus() { return status; }
    public Long getReviewedById() { return reviewedById; }
    public String getReviewedByName() { return reviewedByName; }
    public String getRejectionReason() { return rejectionReason; }
    public String getEmployeeAvatarUrl() { return employeeAvatarUrl; } // Getter for avatar
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
