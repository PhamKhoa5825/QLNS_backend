package com.example.qlns.DTO.Response;

public class EmployeeAttendanceStatsDTO {
    private Long employeeId;
    private String employeeName;
    private long onTime;
    private long late;
    private long absent;

    public EmployeeAttendanceStatsDTO(Long employeeId, String employeeName, long onTime, long late, long absent) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.onTime = onTime;
        this.late = late;
        this.absent = absent;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public long getOnTime() { return onTime; }
    public void setOnTime(long onTime) { this.onTime = onTime; }
    public long getLate() { return late; }
    public void setLate(long late) { this.late = late; }
    public long getAbsent() { return absent; }
    public void setAbsent(long absent) { this.absent = absent; }
}
