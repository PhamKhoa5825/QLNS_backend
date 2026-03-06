package com.example.qlns.DTO;

import com.example.qlns.Entity.*;

// =============================================
// 1. DEPARTMENT DTO
// TV1 dùng
// =============================================
public class DepartmentDTO {
    private Long id;
    private String name;
    private String description;
    private String managerName;     // Chỉ lấy tên, không trả cả object Manager
    private Long managerId;
    private int employeeCount;

    // Convert từ Entity sang DTO
    public static DepartmentDTO from(Department dept, int employeeCount) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.id = dept.getId();
        dto.name = dept.getName();
        dto.description = dept.getDescription();
        dto.employeeCount = employeeCount;
        if (dept.getManager() != null) {
            dto.managerName = dept.getManager().getFullName();
            dto.managerId   = dept.getManager().getId();
        }
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getManagerName() { return managerName; }
    public Long getManagerId() { return managerId; }
    public int getEmployeeCount() { return employeeCount; }
}

