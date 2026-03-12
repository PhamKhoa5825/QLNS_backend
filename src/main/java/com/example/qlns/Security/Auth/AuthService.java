package com.example.qlns.Security.Auth;

import com.example.qlns.DTO.Request.AuthenticationRequest;
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

            // Fetch user from database to get email
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Return authentication response
            return new AuthenticationResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
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

        // Return authentication response
        return new AuthenticationResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
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
}



