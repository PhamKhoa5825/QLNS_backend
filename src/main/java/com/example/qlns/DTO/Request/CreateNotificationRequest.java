package com.example.qlns.DTO.Request;

public class CreateNotificationRequest {
    private String title;
    private String content;
    private String targetType;      // COMPANY / DEPARTMENT / EMPLOYEE
    private Long departmentId;      // dùng khi targetType = DEPARTMENT
    private Long targetEmployeeId;  // THÊM: dùng khi targetType = EMPLOYEE
    private Long createdById;

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTargetType() { return targetType; }
    public Long getDepartmentId() { return departmentId; }
    public Long getTargetEmployeeId() { return targetEmployeeId; }
    public Long getCreatedById() { return createdById; }
}