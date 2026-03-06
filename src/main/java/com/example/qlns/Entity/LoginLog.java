package com.example.qlns.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_logs")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // ← vẫn dùng User (log tài khoản)

    private String ipAddress;
    private String deviceInfo;
    private boolean isSuccess;

    @CreationTimestamp
    private LocalDateTime loginAt;

    public LoginLog() {}

    public LoginLog(User user, String ipAddress, String deviceInfo, boolean isSuccess) {
        this.user = user;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.isSuccess = isSuccess;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ip) { this.ipAddress = ip; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String info) { this.deviceInfo = info; }
    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { isSuccess = success; }
    public LocalDateTime getLoginAt() { return loginAt; }
}
