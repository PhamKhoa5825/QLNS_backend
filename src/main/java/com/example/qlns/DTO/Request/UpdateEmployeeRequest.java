package com.example.qlns.DTO.Request;

public class UpdateEmployeeRequest {
    private String fullName;
    private String phone;
    private String address;
    private String position;
    private Long departmentId;
    private String status;          // ACTIVE / RESIGNED
    private String avatarUrl;

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getPosition() {
        return position;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getStatus() {
        return status;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    private Double baseSalary;

    public Double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(Double baseSalary) {
        this.baseSalary = baseSalary;
    }
}
