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

// Dịch vụ xác thực
// Xử lý đăng nhập và đăng ký người dùng với việc tạo JWT token
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

    // Xác thực người dùng và tạo JWT token
    public AuthenticationResponse login(AuthenticationRequest request) throws AuthenticationException {
        try {
            // Xác thực thông tin người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Lấy thông tin người dùng đã xác thực
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Tạo JWT token
            String token = jwtService.generateToken(userDetails);

            // Lấy người dùng từ database để lấy email
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

            // Trả về kết quả xác thực
            return new AuthenticationResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác", e);
        }
    }

    // Đăng ký người dùng mới
    // Kiểm tra tên đăng nhập và email không bị trùng
    // Mã hóa mật khẩu bằng BCrypt
    @Transactional
    public AuthenticationResponse register(UserRegistrationRequest request) {
        // Kiểm tra xem tên đăng nhập đã tồn tại chưa
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateException("Tên đăng nhập đã được sử dụng: " + request.getUsername());
        }

        // Kiểm tra xem email đã được đăng ký chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateException("Email đã được đăng ký: " + request.getEmail());
        }

        // Tạo người dùng mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa mật khẩu
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        // Lưu người dùng vào database
        User savedUser = userRepository.save(user);

        // Tạo UserDetailsImpl
        UserDetailsImpl userDetails = new UserDetailsImpl(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getPassword(),
                savedUser.getRole()
        );

        // Tạo JWT token
        String token = jwtService.generateToken(userDetails);

        // Trả về kết quả xác thực
        return new AuthenticationResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    // Kiểm tra tính hợp lệ của JWT token
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

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

    // Thay đổi mật khẩu người dùng
    // Kiểm tra mật khẩu cũ trước khi cập nhật mật khẩu mới đã mã hóa
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Mật khẩu cũ không chính xác");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}



