package com.example.qlns.Security.Auth;

import com.example.qlns.DTO.Request.AuthenticationRequest;
import com.example.qlns.DTO.Request.ChangePasswordRequest;
import com.example.qlns.DTO.Request.UserRegistrationRequest;
import com.example.qlns.DTO.Response.AuthenticationResponse;
import com.example.qlns.Entity.User;
import com.example.qlns.Enum.UserStatus;
import com.example.qlns.Exception.DuplicateException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.UserRepository;
import com.example.qlns.Security.JwtService;
import com.example.qlns.Security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * Handles user login and registration with JWT token generation
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.example.qlns.Repository.EmployeeRepository empRepo;

    @Autowired
    private com.example.qlns.Service.SystemLogService logService;

    /**
     * Authenticate user and generate JWT token
     */
    public AuthenticationResponse login(AuthenticationRequest request) throws AuthenticationException {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get authenticated user
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtService.generateToken(userDetails);

            // Fetch user and employee data to get IDs and links
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Long employeeId = user.getEmployeeId();
            Long departmentId = (employeeId != null) ? empRepo.findDepartmentIdByEmployeeId(employeeId) : null;

            // Log activity
            logService.log(user, "LOGIN", "Người dùng " + user.getUsername() + " đăng nhập vào hệ thống");

            // Return authentication response with all required fields for frontend context
            return new AuthenticationResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    departmentId,
                    employeeId
            );

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password", e);
        }
    }

    /**
     * Register new user
     * Validates that username and email are not already in use
     * Hashes password using BCrypt
     */
    @Transactional
    public AuthenticationResponse register(UserRegistrationRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateException("Username already taken: " + request.getUsername());
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateException("Email already registered: " + request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        // Save user to database
        User savedUser = userRepository.save(user);

        // Create UserDetailsImpl
        UserDetailsImpl userDetails = new UserDetailsImpl(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getPassword(),
                savedUser.getRole()
        );

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);

        Long departmentId = (savedUser.getEmployeeId() != null) ? empRepo.findDepartmentIdByEmployeeId(savedUser.getEmployeeId()) : null;

        // Return authentication response
        return new AuthenticationResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                departmentId,
                savedUser.getEmployeeId()
        );
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            UserDetailsImpl userDetails = new UserDetailsImpl(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole()
            );

            return jwtService.validateToken(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Change user password
     * Validates old password, checks that new password matches confirm password
     * Then updates password in database
     * 
     * @param username the username of the user changing password
     * @param request contains old password, new password, and confirm password
     * @return success message
     */
    @Transactional
    public String changePassword(String username, ChangePasswordRequest request) {
        // Validate that new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadCredentialsException("New password and confirm password do not match");
        }

        // Validate that new password is not same as old password
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadCredentialsException("New password must be different from old password");
        }

        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        // Encode new password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        // Update password
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);

        // Log activity
        logService.log(user, "UPDATE", "Người dùng " + username + " đã đổi mật khẩu");

        return "Password changed successfully";
    }
}



