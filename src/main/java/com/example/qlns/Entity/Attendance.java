package com.example.qlns.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;          // ← đổi từ User sang Employee

    @Column(nullable = false)
    private LocalDate date;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double latitude;
    private Double longitude;
    private Double distanceFromOffice;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum AttendanceStatus {
        PRESENT,    // Có mặt (bao gồm đi trễ nếu không cần phân biệt)
        LATE,       // Đi trễ
        ABSENT,     // Vắng mặt không phép
        LEAVE       // Nghỉ phép có phép
    }

    public Attendance() {}

    public Attendance(Employee employee, LocalDate date, LocalDateTime checkInTime,
                      Double lat, Double lng, Double distance) {
        this.employee = employee;
        this.date = date;
        this.checkInTime = checkInTime;
        this.latitude = lat;
        this.longitude = lng;
        this.distanceFromOffice = distance;
    }

    public Long getId() { return id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime t) { this.checkInTime = t; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime t) { this.checkOutTime = t; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getDistanceFromOffice() { return distanceFromOffice; }
    public void setDistanceFromOffice(Double d) { this.distanceFromOffice = d; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}