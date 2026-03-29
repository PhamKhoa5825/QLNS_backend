package com.example.qlns.Entity;

import com.example.qlns.Enum.LeaveSession;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "request_details")
public class RequestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Column(name = "specific_date", nullable = false)
    private LocalDate specificDate;

    // Chỉ dùng cho LEAVE
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_session")
    private LeaveSession leaveSession;

    // Chỉ dùng cho OVERTIME
    @Column(name = "overtime_hours")
    private Double overtimeHours;

    // Chỉ dùng cho PUNCH_CORRECTION
    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId() { return id; }
    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }
    public LocalDate getSpecificDate() { return specificDate; }
    public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }
    public LeaveSession getLeaveSession() { return leaveSession; }
    public void setLeaveSession(LeaveSession leaveSession) { this.leaveSession = leaveSession; }
    public Double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }
    public LocalTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalTime checkIn) { this.checkIn = checkIn; }
    public LocalTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalTime checkOut) { this.checkOut = checkOut; }
}
