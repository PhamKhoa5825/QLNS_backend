package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.LeaveRequest;

public class LeaveRequestDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String reason;
    private String status;
    private String rejectionReason;
    private String approvedByName;
    private String createdAt;

    public static LeaveRequestDTO from(LeaveRequest lr) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.id = lr.getId();
        dto.leaveType = lr.getLeaveType().name();
        dto.startDate = lr.getStartDate().toString();
        dto.endDate = lr.getEndDate().toString();
        dto.reason = lr.getReason();
        dto.status = lr.getStatus().name();
        dto.rejectionReason = lr.getRejectionReason();
        dto.createdAt = lr.getCreatedAt() != null ? lr.getCreatedAt().toString() : null;
        if (lr.getEmployee() != null) {
            dto.employeeId = lr.getEmployee().getId();
            dto.employeeName = lr.getEmployee().getFullName();
        }
        if (lr.getApprovedBy() != null) {
            dto.approvedByName = lr.getApprovedBy().getFullName();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
