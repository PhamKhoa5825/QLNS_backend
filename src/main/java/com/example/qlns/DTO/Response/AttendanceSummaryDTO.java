package com.example.qlns.DTO.Response;

public class AttendanceSummaryDTO {
    private String date; // YYYY-MM-DD
    private String status; // PRESENT, LATE, LEAVE, OT, TRIP, ABSENT, WEEKEND
    private String description;
    private String colorCode;
    private String topColor;
    private String middleColor;
    private String bottomColor;
    private Double salaryImpact; // Gain or Loss for this day
    private Boolean isPaid; // Whether this day is considered a paid workday
    private Double dayValue; // 1.0 or 0.5
    private String borderColor; // New: for OT border
    private Boolean isBold = false; // New: to highlight Today

    public AttendanceSummaryDTO() {
    }

    public AttendanceSummaryDTO(String date, String status, String description, String colorCode) {
        this.date = date;
        this.status = status;
        this.description = description;
        this.colorCode = colorCode;
    }

    public AttendanceSummaryDTO(String date, String status, String description, String colorCode,
            Double salaryImpact, Boolean isPaid, Double dayValue) {
        this.date = date;
        this.status = status;
        this.description = description;
        this.colorCode = colorCode;
        this.salaryImpact = salaryImpact;
        this.isPaid = isPaid;
        this.dayValue = dayValue;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public Double getSalaryImpact() {
        return salaryImpact;
    }

    public void setSalaryImpact(Double salaryImpact) {
        this.salaryImpact = salaryImpact;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Double getDayValue() {
        return dayValue;
    }

    public void setDayValue(Double dayValue) {
        this.dayValue = dayValue;
    }

    public String getTopColor() {
        return topColor;
    }

    public void setTopColor(String topColor) {
        this.topColor = topColor;
    }

    public String getMiddleColor() {
        return middleColor;
    }

    public void setMiddleColor(String middleColor) {
        this.middleColor = middleColor;
    }

    public String getBottomColor() {
        return bottomColor;
    }

    public void setBottomColor(String bottomColor) {
        this.bottomColor = bottomColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public Boolean getIsBold() {
        return isBold;
    }

    public void setIsBold(Boolean isBold) {
        this.isBold = isBold;
    }
}
