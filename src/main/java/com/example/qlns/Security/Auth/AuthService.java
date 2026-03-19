package com.example.qlns.Security.Auth;

import com.example.qlns.DTO.Request.AuthenticationRequest;
import com.example.qlns.DTO.Request.UserRegistrationRequest;
import com.example.qlns.DTO.Response.AuthenticationResponse;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.User;
import com.example.qlns.Enum.UserStatus;
import com.example.qlns.Exception.DuplicateException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.EmployeeRepository;
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

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService jwtService;

    /**
     * Login: trả về token + userId + employeeId + fullName + avatarUrl
     */
    @Transactional(readOnly = true)
    public AuthenticationResponse login(AuthenticationRequest request) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Tìm Employee tương ứng qua employeeId hoặc email
            Long employeeId = null;
            String fullName = user.getUsername();
            String avatarUrl = null;

            // Cách 1: User có field employeeId
            if (user.getEmployeeId() != null) {
                Employee emp = employeeRepository.findById(user.getEmployeeId()).orElse(null);
                if (emp != null) {
                    employeeId = emp.getId();
                    fullName = emp.getFullName();
                    avatarUrl = emp.getAvatarUrl();
                }
            }

            // Cách 2: Fallback tìm theo email
            if (employeeId == null && user.getEmail() != null) {
                Employee emp = employeeRepository.findByEmail(user.getEmail()).orElse(null);
                if (emp != null) {
                    employeeId = emp.getId();
                    fullName = emp.getFullName();
                    avatarUrl = emp.getAvatarUrl();
                }
            }

            return new AuthenticationResponse(
                    token,
                    user.getId(),
                    employeeId,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name(),
                    fullName,
                    avatarUrl
            );

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password", e);
        }
    }

    @Transactional
    public AuthenticationResponse register(UserRegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateException("Username already taken: " + request.getUsername());
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(
                savedUser.getId(), savedUser.getUsername(),
                savedUser.getPassword(), savedUser.getRole()
        );
        String token = jwtService.generateToken(userDetails);

        return new AuthenticationResponse(
                token,
                savedUser.getId(),
                null,  // chưa có employee khi register
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getUsername(),
                null
        );
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            UserDetailsImpl userDetails = new UserDetailsImpl(
                    user.getId(), user.getUsername(),
                    user.getPassword(), user.getRole()
            );
            return jwtService.validateToken(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }
}