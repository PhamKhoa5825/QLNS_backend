package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.LeaveBalance;

public class LeaveBalanceDTO {
    private Long employeeId;
    private Integer year;
    private Integer totalDays;
    private Integer usedDays;
    private Integer remainingDays;

    public static LeaveBalanceDTO from(LeaveBalance lb) {
        LeaveBalanceDTO dto = new LeaveBalanceDTO();
        dto.employeeId = lb.getEmployee().getId();
        dto.year = lb.getYear();
        dto.totalDays = lb.getTotalDays();
        dto.usedDays = lb.getUsedDays();
        dto.remainingDays = lb.getRemainingDays();
        return dto;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public Integer getUsedDays() {
        return usedDays;
    }

    public Integer getRemainingDays() {
        return remainingDays;
    }
}
