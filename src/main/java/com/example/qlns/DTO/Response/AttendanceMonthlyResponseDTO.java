package com.example.qlns.DTO.Response;

import java.util.List;

public class AttendanceMonthlyResponseDTO {
    private List<AttendanceSummaryDTO> days;
    private Double baseSalary;
    private Double estimatedSalarySoFar;
    private Double totalDeductions;
    private Double totalBonuses;
    private Integer standardWorkingDays;

    public AttendanceMonthlyResponseDTO() {}

    // Getters and Setters
    public List<AttendanceSummaryDTO> getDays() { return days; }
    public void setDays(List<AttendanceSummaryDTO> days) { this.days = days; }
    public Double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(Double baseSalary) { this.baseSalary = baseSalary; }
    public Double getEstimatedSalarySoFar() { return estimatedSalarySoFar; }
    public void setEstimatedSalarySoFar(Double estimatedSalarySoFar) { this.estimatedSalarySoFar = estimatedSalarySoFar; }
    public Double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(Double totalDeductions) { this.totalDeductions = totalDeductions; }
    public Double getTotalBonuses() { return totalBonuses; }
    public void setTotalBonuses(Double totalBonuses) { this.totalBonuses = totalBonuses; }
    public Integer getStandardWorkingDays() { return standardWorkingDays; }
    public void setStandardWorkingDays(Integer standardWorkingDays) { this.standardWorkingDays = standardWorkingDays; }
}
