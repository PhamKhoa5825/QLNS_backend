package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Employee;

public class EmployeeDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String dateOfBirth;
    private String gender;
    private String avatarUrl;
    private String position;
    private String joinDate;
    private String status;
    private Long departmentId;
    private String departmentName;
    private String role;            // Từ bảng users

    public static EmployeeDTO from(Employee emp) {
        return from(emp, null);
    }

    public static EmployeeDTO from(Employee emp, String role) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = emp.getId();
        dto.fullName = emp.getFullName();
        dto.email = emp.getEmail();
        dto.phone = emp.getPhone();
        dto.address = emp.getAddress();
        dto.dateOfBirth = emp.getDateOfBirth() != null ? emp.getDateOfBirth().toString() : null;
        dto.gender = emp.getGender() != null ? emp.getGender().name() : null;
        dto.avatarUrl = emp.getAvatarUrl();
        dto.position = emp.getPosition();
        dto.joinDate = emp.getJoinDate() != null ? emp.getJoinDate().toString() : null;
        dto.status = emp.getStatus().name();
        dto.role = role;
        if (emp.getDepartment() != null) {
            dto.departmentId = emp.getDepartment().getId();
            dto.departmentName = emp.getDepartment().getName();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getPosition() {
        return position;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public String getStatus() {
        return status;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getRole() {
        return role;
    }
}
