package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.ChangePasswordRequest;
import com.example.qlns.DTO.Request.AuthenticationRequest;
import com.example.qlns.DTO.Request.UserRegistrationRequest;
import com.example.qlns.DTO.Response.AuthenticationResponse;
import com.example.qlns.Security.Auth.AuthService;
import com.example.qlns.Security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Bộ điều khiển xác thực
// Xử lý các endpoint đăng nhập và đăng ký người dùng
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/login
    // Xác thực người dùng và trả về JWT token
    // @param request: tên đăng nhập và mật khẩu
    // @return: AuthenticationResponse chứa JWT token
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/register
    // Đăng ký người dùng mới
    // @param request: thông tin đăng ký người dùng
    // @return: AuthenticationResponse chứa JWT token
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        AuthenticationResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/auth/validate
    // Kiểm tra tính hợp lệ của JWT token (yêu cầu xác thực)
    // @return: thông báo thành công hoặc lỗi
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        // Lấy token từ header Authorization
        String authHeader = ((org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null)
            ? ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization")
            : null);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (authService.validateToken(token)) {
                return ResponseEntity.ok("Token hợp lệ");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ");
    }

    // POST /api/auth/refresh
    // Làm mới JWT token (yêu cầu xác thực)
    // Có thể dùng để lấy token mới trước khi token cũ hết hạn
    // @return: JWT token mới
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken() {
        return ResponseEntity.ok("Endpoint làm mới token");
    }

    // POST /api/auth/change-password
    // Đổi mật khẩu cho người dùng hiện tại đã xác thực
    // @param request: mật khẩu cũ và mới
    // @param authentication: thông tin người dùng đã xác thực
    // @return: thông báo thành công
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        authService.changePassword(userDetails.getUserId(), request);
        
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}
