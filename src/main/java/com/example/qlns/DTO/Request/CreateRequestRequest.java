package com.example.qlns.DTO.Request;

public class CreateRequestRequest {
    private String title;           // Bắt buộc
    private String description;     // Tuỳ chọn
    private String fileUrl;         // Tuỳ chọn (link sau upload)
    private String fileName;        // Tuỳ chọn

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFileUrl() { return fileUrl; }
    public String getFileName() { return fileName; }
}
