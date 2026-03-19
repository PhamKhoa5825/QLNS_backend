package com.example.qlns.DTO.Response;

import com.example.qlns.Enum.Role;

public class AuthenticationResponse {

    private String token;
    private Long userId;
    private Long employeeId;        // THÊM: Employee.id (Android cần)
    private String username;
    private String email;
    private String role;             // ĐỔI: String thay vì Role enum → Gson parse dễ
    private String fullName;         // THÊM: tên nhân viên
    private String avatarUrl;        // THÊM: URL avatar
    private String tokenType = "Bearer";

    public AuthenticationResponse() {}

    public AuthenticationResponse(String token, Long userId, Long employeeId,
                                  String username, String email, String role,
                                  String fullName, String avatarUrl) {
        this.token = token;
        this.userId = userId;
        this.employeeId = employeeId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

    // Getters & Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}