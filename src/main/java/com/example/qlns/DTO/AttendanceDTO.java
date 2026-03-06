package com.example.qlns.DTO;

import com.example.qlns.Entity.Attendance;

// =============================================
// 3. ATTENDANCE DTO
// TV3 dùng
// =============================================
public class AttendanceDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String date;
    private String checkInTime;
    private String checkOutTime;
    private Double distanceFromOffice;
    private String status;
    private String note;

    public static AttendanceDTO from(Attendance att) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.id = att.getId();
        dto.date = att.getDate().toString();
        dto.distanceFromOffice = att.getDistanceFromOffice();
        dto.status = att.getStatus().name();
        dto.note = att.getNote();
        dto.checkInTime = att.getCheckInTime() != null ? att.getCheckInTime().toString() : null;
        dto.checkOutTime = att.getCheckOutTime() != null ? att.getCheckOutTime().toString() : null;

        if (att.getEmployee() != null) {
            dto.employeeId = att.getEmployee().getId();
            dto.employeeName = att.getEmployee().getFullName();
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDate() {
        return date;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public Double getDistanceFromOffice() {
        return distanceFromOffice;
    }

    public String getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }
}
