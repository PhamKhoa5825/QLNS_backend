package com.example.qlns.DTO.Response;

import com.example.qlns.Entity.*;

// =============================================
// REQUEST DTOs (Android → Server)
// =============================================


// =============================================
// RESPONSE DTOs (Server → Android)
// =============================================

// TV3
public class AttendanceDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String date;
    private String checkIn;
    private String checkOut;
    private Float workHours;
    private Double locationLat;
    private Double locationLng;
    private String status;
    private Integer lateMinutes;

    public static AttendanceDTO from(Attendance a) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.id = a.getId();
        dto.date = a.getDate().toString();
        dto.workHours = a.getWorkHours();
        dto.locationLat = a.getLocationLat();
        dto.locationLng = a.getLocationLng();
        dto.status = a.getStatus().name();
        dto.checkIn = a.getCheckIn() != null ? a.getCheckIn().toString() : null;
        dto.checkOut = a.getCheckOut() != null ? a.getCheckOut().toString() : null;
        if (a.getEmployee() != null) {
            dto.employeeId = a.getEmployee().getId();
            dto.employeeName = a.getEmployee().getFullName();
        }
        dto.lateMinutes = a.getLateMinutes();
        return dto;
    }
    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getDate() { return date; }
    public String getCheckIn() { return checkIn; }
    public String getCheckOut() { return checkOut; }
    public Float getWorkHours() { return workHours; }
    public Double getLocationLat() { return locationLat; }
    public Double getLocationLng() { return locationLng; }
    public String getStatus() { return status; }
    public Integer getLateMinutes() { return lateMinutes; }
}

