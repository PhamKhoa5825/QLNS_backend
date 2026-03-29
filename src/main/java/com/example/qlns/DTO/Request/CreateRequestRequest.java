package com.example.qlns.DTO.Request;

public class CreateRequestRequest {
    private String title;           // Bắt buộc
    private String description;     // Tuỳ chọn
    private String fileUrl;         // Tuỳ chọn (link sau upload)
    private String fileName;        // Tuỳ chọn
    private com.example.qlns.Enum.RequestType type;
    private java.util.List<com.example.qlns.DTO.Response.RequestDetailDTO> details;

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFileUrl() { return fileUrl; }
    public String getFileName() { return fileName; }

    public com.example.qlns.Enum.RequestType getType() {
        return type;
    }

    public java.util.List<com.example.qlns.DTO.Response.RequestDetailDTO> getDetails() {
        return details;
    }
}
