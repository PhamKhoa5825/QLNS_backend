package com.example.qlns.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "company_settings")
public class CompanySettings {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    @Column(name = "base_lat", nullable = false)
    private Double baseLat = 10.7769;   // Mặc định TP.HCM

    @Column(name = "base_lng", nullable = false)
    private Double baseLng = 106.7009;

    @Column(name = "allowed_radius", nullable = false)
    private Integer allowedRadius = 1000;   // Mét

    @Column(name = "work_start_time", nullable = false)
    private String workStartTime = "08:00"; // HH:mm

    @Column(name = "morning_end_time", nullable = false)
    private String morningEndTime = "12:00"; // Giờ kết thúc ca sáng

    @Column(name = "afternoon_start_time", nullable = false)
    private String afternoonStartTime = "13:00"; // Giờ bắt đầu ca chiều

    @Column(name = "work_end_time", nullable = false)
    private String workEndTime = "17:00";

    public CompanySettings() {}

    public Long getId() { return id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public Double getBaseLat() { return baseLat; }
    public void setBaseLat(Double baseLat) { this.baseLat = baseLat; }
    public Double getBaseLng() { return baseLng; }
    public void setBaseLng(Double baseLng) { this.baseLng = baseLng; }
    public Integer getAllowedRadius() { return allowedRadius; }
    public void setAllowedRadius(Integer allowedRadius) { this.allowedRadius = allowedRadius; }
    public String getWorkStartTime() { return workStartTime; }
    public void setWorkStartTime(String workStartTime) { this.workStartTime = workStartTime; }
    public String getMorningEndTime() { return morningEndTime; }
    public void setMorningEndTime(String morningEndTime) { this.morningEndTime = morningEndTime; }
    public String getAfternoonStartTime() { return afternoonStartTime; }
    public void setAfternoonStartTime(String afternoonStartTime) { this.afternoonStartTime = afternoonStartTime; }
    public String getWorkEndTime() { return workEndTime; }
    public void setWorkEndTime(String workEndTime) { this.workEndTime = workEndTime; }
}
