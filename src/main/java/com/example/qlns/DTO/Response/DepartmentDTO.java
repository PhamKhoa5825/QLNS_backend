package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Department;

// TV1
public class DepartmentDTO {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private int employeeCount;
    private String createdAt;

    public static DepartmentDTO from(Department dept, int employeeCount) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.id = dept.getId();
        dto.name = dept.getName();
        dto.description = dept.getDescription();
        dto.employeeCount = employeeCount;
        dto.createdAt = dept.getCreatedAt() != null ? dept.getCreatedAt().toString() : null;
        if (dept.getManager() != null) {
            dto.managerId = dept.getManager().getId();
            dto.managerName = dept.getManager().getFullName();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
