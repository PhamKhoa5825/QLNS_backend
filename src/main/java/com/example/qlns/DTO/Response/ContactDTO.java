package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Employee;

// [Chat] Danh bạ thông minh - DTO hiển thị thông tin đồng nghiệp
public class ContactDTO {

    private Long employeeId;
    private Long userId;
    private String fullName;
    private String position;
    private String departmentName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;      // ACTIVE, ON_LEAVE, BUSINESS_TRIP
    private String skills;

    // [Chat] Factory method từ Employee entity
    public static ContactDTO from(Employee emp, Long userId, String userStatus) {
        ContactDTO dto = new ContactDTO();
        dto.employeeId = emp.getId();
        dto.userId = userId;
        dto.fullName = emp.getFullName();
        dto.position = emp.getPosition();
        dto.email = emp.getEmail();
        dto.phone = emp.getPhone();
        dto.avatarUrl = emp.getAvatarUrl();
        dto.skills = emp.getSkills();
        dto.status = userStatus;
        if (emp.getDepartment() != null) {
            dto.departmentName = emp.getDepartment().getName();
        }
        return dto;
    }

    // ── Getters ──────────────────────────────────────

    public Long getEmployeeId() { return employeeId; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getPosition() { return position; }
    public String getDepartmentName() { return departmentName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
    public String getSkills() { return skills; }
}
