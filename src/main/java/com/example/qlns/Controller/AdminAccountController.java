package com.example.qlns.Controller;

import com.example.qlns.Entity.User;
import com.example.qlns.Enum.UserStatus;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * AdminAccountController — Quản lý tài khoản (Admin-only)
 */
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PutMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long userId,
            @RequestParam("newPassword") String newPassword) {
        
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản để reset."));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateAccountStatus(
            @PathVariable Long userId,
            @RequestParam("status") String status) {
        
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản."));
        
        try {
            user.setStatus(UserStatus.valueOf(status.toUpperCase()));
            userRepo.save(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
}
