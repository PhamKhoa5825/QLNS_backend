package com.example.qlns.Controller;

import com.example.qlns.Security.Auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accounts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminAccountController {

    @Autowired
    private AuthService authService;

    @PutMapping("/{userId}/status")
    public ResponseEntity<Void> updateAccountStatus(@PathVariable("userId") Long userId,
                                                    @RequestParam("status") String status) {
        authService.updateAccountStatus(userId, status);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable("userId") Long userId,
                                              @RequestParam("newPassword") String newPassword) {
        authService.resetPassword(userId, newPassword);
        return ResponseEntity.ok().build();
    }
}
