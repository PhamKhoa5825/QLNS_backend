package com.example.qlns.DTO;

import com.example.qlns.Entity.Employee;

// =============================================
// 2. EMPLOYEE DTO
// TV1 dùng
// =============================================
public class EmployeeDTO {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String phone;
    private String position;
    private String avatarUrl;
    private Double salary;
    private String contractType;
    private String gender;
    private String startDate;
    private String endDate;
    private String email;           // Từ bảng User
    private String role;            // Từ bảng User
    private Long departmentId;
    private String departmentName;

    public static EmployeeDTO from(Employee emp) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = emp.getId();
        dto.employeeCode = emp.getEmployeeCode();
        dto.fullName = emp.getFullName();
        dto.phone = emp.getPhone();
        dto.position = emp.getPosition();
        dto.avatarUrl = emp.getAvatarUrl();
        dto.salary = emp.getSalary();
        dto.contractType = emp.getContractType() != null ? emp.getContractType().name() : null;
        dto.gender = emp.getGender() != null ? emp.getGender().name() : null;
        dto.startDate = emp.getStartDate() != null ? emp.getStartDate().toString() : null;
        dto.endDate = emp.getEndDate() != null ? emp.getEndDate().toString() : null;

        // Lấy thông tin từ User (không trả cả object User)
        if (emp.getUser() != null) {
            dto.email = emp.getUser().getEmail();
            dto.role = emp.getUser().getRole().name();
        }

        // Lấy thông tin từ Department (không trả cả object Department)
        if (emp.getDepartment() != null) {
            dto.departmentId = emp.getDepartment().getId();
            dto.departmentName = emp.getDepartment().getName();
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getPosition() {
        return position;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Double getSalary() {
        return salary;
    }

    public String getContractType() {
        return contractType;
    }

    public String getGender() {
        return gender;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }
}
