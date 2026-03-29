package com.example.qlns.DTO.Response;

public class AttendanceStatsDTO {
    private Double totalWorkHours;
    private Long onTimeCount;
    private Long lateCount;
    private Long absentCount;
    private Double averageHoursPerDay;

    public AttendanceStatsDTO(Double totalWorkHours, Long onTimeCount, Long lateCount, Long absentCount, Double averageHoursPerDay) {
        this.totalWorkHours = totalWorkHours;
        this.onTimeCount = onTimeCount;
        this.lateCount = lateCount;
        this.absentCount = absentCount;
        this.averageHoursPerDay = averageHoursPerDay;
    }

    // Getters
    public Double getTotalWorkHours() { return totalWorkHours; }
    public Long getOnTimeCount() { return onTimeCount; }
    public Long getLateCount() { return lateCount; }
    public Long getAbsentCount() { return absentCount; }
    public Double getAverageHoursPerDay() { return averageHoursPerDay; }
}
