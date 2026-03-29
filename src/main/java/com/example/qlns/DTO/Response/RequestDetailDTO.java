package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.RequestDetail;
import com.example.qlns.Enum.LeaveSession;
import java.time.LocalDate;

public class RequestDetailDTO {
    private Long id;
    private LocalDate specificDate;
    private LeaveSession leaveSession;
    private Double overtimeHours;
    private String checkIn;
    private String checkOut;

    public static RequestDetailDTO from(RequestDetail d) {
        RequestDetailDTO dto = new RequestDetailDTO();
        dto.id = d.getId();
        dto.specificDate = d.getSpecificDate();
        dto.leaveSession = d.getLeaveSession();
        dto.overtimeHours = d.getOvertimeHours();
        
        if (d.getCheckIn() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            dto.checkIn = d.getCheckIn().format(formatter);
        }
        if (d.getCheckOut() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            dto.checkOut = d.getCheckOut().format(formatter);
        }
        
        return dto;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getSpecificDate() { return specificDate; }
    public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }
    public LeaveSession getLeaveSession() { return leaveSession; }
    public void setLeaveSession(LeaveSession leaveSession) { this.leaveSession = leaveSession; }
    public Double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }
    public String getCheckIn() { return checkIn; }
    public void setCheckIn(String checkIn) { this.checkIn = checkIn; }
    public String getCheckOut() { return checkOut; }
    public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
}
