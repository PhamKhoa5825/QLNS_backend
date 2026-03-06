package com.example.qlns.Service;

import com.example.qlns.Entity.User;
import com.example.qlns.Enum.Role;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.UserRepository;
import org.springframework.stereotype.Service;

// TV2 sở hữu - chỉ xử lý tài khoản
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Tìm theo email (TV2 dùng khi login)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", id));
    }

    // Khóa/mở tài khoản
    public void setActive(Long id, boolean active) {
        User user = getById(id);
        user.setActive(active);
        userRepository.save(user);
    }

    // Đổi role
    public User updateRole(Long id, Role role) {
        User user = getById(id);
        user.setRole(role);
        return userRepository.save(user);
    }

    // Đổi password (TV2 dùng)
    public void changePassword(Long id, String newHashedPassword) {
        User user = getById(id);
        user.setPassword(newHashedPassword);
        userRepository.save(user);
    }
}