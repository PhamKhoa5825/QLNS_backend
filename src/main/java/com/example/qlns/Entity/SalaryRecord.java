package com.example.qlns.Entity;

import com.example.qlns.Enum.SalaryRecordStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_records", uniqueConstraints = @UniqueConstraint(columnNames = { "employee_id", "month", "year" }))
public class SalaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    // ── Attendance Snapshot ────────────────────────
    private Integer workingDaysStandard = 22; // Ngày công chuẩn tháng đó
    private Double daysWorked = 0.0; // Ngày thực tế đi làm (hỗ trợ 0.5)
    private Double daysAbsentExcused = 0.0; // Nghỉ có phép (hỗ trợ 0.5)
    private Double daysAbsentUnexcused = 0.0; // Nghỉ không phép (hỗ trợ 0.5)
    private Integer totalLateMinutes = 0; // Tổng phút đi muộn
    private Double totalOvertimeHours = 0.0; // Tổng giờ tăng ca
    private Double daysSick = 0.0; // Số ngày nghỉ ốm (BHXH chi trả)
    private Double businessTripDays = 0.0; // Ngày đi công tác

    // ── Salary Calculation ─────────────────────────
    private Double baseSalary = 0.0; // Snapshot lương cơ bản tại thời điểm tính
    private Double deductionLate = 0.0; // Khấu trừ đi muộn
    private Double deductionUnexcused = 0.0; // Khấu trừ nghỉ không phép
    private Double taskBonus = 0.0; // Thưởng hiệu suất task
    private Double overtimeBonus = 0.0; // Thưởng tăng ca
    private Double deductionSick = 0.0; // Khấu trừ nghỉ ốm (BHXH chi trả)
    private Double grossSalary = 0.0; // Thực nhận

    // ── Performance ────────────────────────────────
    private Double performanceScore = 0.0; // Điểm tổng (0-100)
    private String performanceGrade = "D"; // S/A/B/C/D

    // ── Metadata ───────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    private SalaryRecordStatus status = SalaryRecordStatus.DRAFT;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public SalaryRecord() {
    }

    // ── Getters & Setters ──────────────────────────
    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getWorkingDaysStandard() {
        return workingDaysStandard;
    }

    public void setWorkingDaysStandard(Integer workingDaysStandard) {
        this.workingDaysStandard = workingDaysStandard;
    }

    public Double getDaysWorked() {
        return daysWorked;
    }

    public void setDaysWorked(Double daysWorked) {
        this.daysWorked = daysWorked;
    }

    public Double getDaysAbsentExcused() {
        return daysAbsentExcused;
    }

    public void setDaysAbsentExcused(Double daysAbsentExcused) {
        this.daysAbsentExcused = daysAbsentExcused;
    }

    public Double getDaysAbsentUnexcused() {
        return daysAbsentUnexcused;
    }

    public void setDaysAbsentUnexcused(Double daysAbsentUnexcused) {
        this.daysAbsentUnexcused = daysAbsentUnexcused;
    }

    public Integer getTotalLateMinutes() {
        return totalLateMinutes;
    }

    public void setTotalLateMinutes(Integer totalLateMinutes) {
        this.totalLateMinutes = totalLateMinutes;
    }

    public Double getTotalOvertimeHours() {
        return totalOvertimeHours;
    }

    public void setTotalOvertimeHours(Double totalOvertimeHours) {
        this.totalOvertimeHours = totalOvertimeHours;
    }

    public Double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(Double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public Double getDeductionLate() {
        return deductionLate;
    }

    public void setDeductionLate(Double deductionLate) {
        this.deductionLate = deductionLate;
    }

    public Double getDeductionUnexcused() {
        return deductionUnexcused;
    }

    public void setDeductionUnexcused(Double deductionUnexcused) {
        this.deductionUnexcused = deductionUnexcused;
    }

    public Double getTaskBonus() {
        return taskBonus;
    }

    public void setTaskBonus(Double taskBonus) {
        this.taskBonus = taskBonus;
    }

    public Double getOvertimeBonus() {
        return overtimeBonus;
    }

    public void setOvertimeBonus(Double overtimeBonus) {
        this.overtimeBonus = overtimeBonus;
    }

    public Double getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(Double grossSalary) {
        this.grossSalary = grossSalary;
    }

    public Double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(Double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public String getPerformanceGrade() {
        return performanceGrade;
    }

    public void setPerformanceGrade(String performanceGrade) {
        this.performanceGrade = performanceGrade;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public SalaryRecordStatus getStatus() {
        return status;
    }

    public void setStatus(SalaryRecordStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Double getDaysSick() {
        return daysSick;
    }

    public void setDaysSick(Double daysSick) {
        this.daysSick = daysSick;
    }

    public Double getDeductionSick() {
        return deductionSick;
    }

    public void setDeductionSick(Double deductionSick) {
        this.deductionSick = deductionSick;
    }

    public Double getBusinessTripDays() {
        return businessTripDays;
    }

    public void setBusinessTripDays(Double businessTripDays) {
        this.businessTripDays = businessTripDays;
    }
}
