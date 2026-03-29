package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.SalaryRecord;

public class SalaryRecordDTO {

    public Long id;
    public Long employeeId;
    public String employeeName;
    public Long departmentId;
    public String departmentName;
    public Integer month;
    public Integer year;

    // Attendance Snapshot
    public Integer workingDaysStandard;
    public Double daysWorked;
    public Double daysAbsentExcused; // Changed to Double
    public Double daysAbsentUnexcused;
    public Integer totalLateMinutes;
    public Double totalOvertimeHours; // New field
    public Double daysSick; // New field
    public Double businessTripDays; // New field

    // Salary Breakdown
    public Double baseSalary;
    public Double deductionLate;
    public Double deductionUnexcused;
    public Double taskBonus;
    public Double overtimeBonus; // New field
    public Double deductionSick; // New field
    public Double grossSalary;

    // Performance
    public Double performanceScore;
    public String performanceGrade;

    // Metadata
    public String note;
    public String status;
    public String createdAt;

    public static SalaryRecordDTO from(SalaryRecord r) {
        SalaryRecordDTO dto = new SalaryRecordDTO();
        if (r == null)
            return dto;

        dto.id = r.getId();

        Employee emp = r.getEmployee();
        if (emp != null) {
            dto.employeeId = emp.getId();
            dto.employeeName = emp.getFullName();
            if (emp.getDepartment() != null) {
                dto.departmentId = emp.getDepartment().getId();
                dto.departmentName = emp.getDepartment().getName();
            }
        }

        dto.month = r.getMonth();
        dto.year = r.getYear();
        dto.workingDaysStandard = r.getWorkingDaysStandard();
        dto.daysWorked = r.getDaysWorked();
        dto.daysAbsentExcused = r.getDaysAbsentExcused();
        dto.daysAbsentUnexcused = r.getDaysAbsentUnexcused();
        dto.totalLateMinutes = r.getTotalLateMinutes();
        dto.totalOvertimeHours = r.getTotalOvertimeHours();
        dto.baseSalary = r.getBaseSalary();
        dto.deductionLate = r.getDeductionLate();
        dto.deductionUnexcused = r.getDeductionUnexcused();
        dto.taskBonus = r.getTaskBonus();
        dto.overtimeBonus = r.getOvertimeBonus();
        dto.daysSick = r.getDaysSick();
        dto.businessTripDays = r.getBusinessTripDays();
        dto.deductionSick = r.getDeductionSick();
        dto.grossSalary = r.getGrossSalary();
        dto.performanceScore = r.getPerformanceScore();
        dto.performanceGrade = r.getPerformanceGrade();
        dto.note = r.getNote();

        if (r.getStatus() != null) {
            dto.status = r.getStatus().name();
        } else {
            dto.status = "DRAFT";
        }

        if (r.getCreatedAt() != null)
            dto.createdAt = r.getCreatedAt().toString();
        return dto;
    }
}
