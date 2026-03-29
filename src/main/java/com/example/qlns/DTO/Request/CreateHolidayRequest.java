package com.example.qlns.DTO.Request;

import java.time.LocalDate;

public class CreateHolidayRequest {
    private LocalDate date;
    private String name;
    private String description;

    // Getters and setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
