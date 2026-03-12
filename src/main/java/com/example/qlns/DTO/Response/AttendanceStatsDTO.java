package com.example.qlns.DTO.Response;

public class AttendanceStatsDTO {
    private long onTime;
    private long late;
    private long absent;
    
    public AttendanceStatsDTO(long onTime, long late, long absent) {
        this.onTime = onTime;
        this.late = late;
        this.absent = absent;
    }

    public long getOnTime() { return onTime; }
    public void setOnTime(long onTime) { this.onTime = onTime; }
    public long getLate() { return late; }
    public void setLate(long late) { this.late = late; }
    public long getAbsent() { return absent; }
    public void setAbsent(long absent) { this.absent = absent; }
}
