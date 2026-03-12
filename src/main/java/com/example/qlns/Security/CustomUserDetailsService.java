package com.example.qlns.Security;

import com.example.qlns.Entity.User;
import com.example.qlns.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService implementation that loads users from database
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username and return UserDetailsImpl
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println("DEBUG encoder type: " + passwordEncoder.getClass().getSimpleName());
        System.out.println("DEBUG matches: " + passwordEncoder.matches("password123", user.getPassword()));

        System.out.println("DEBUG password_hash from DB: [" + user.getPassword() + "]");

//        return new UserDetailsImpl(
//                user.getId(),
//                user.getUsername(),
//                user.getPassword(),
//                user.getRole()
//        );
        UserDetails ud = new UserDetailsImpl(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
        System.out.println("DEBUG UserDetailsImpl.getPassword(): [" + ud.getPassword() + "]");
        return ud;
    }

    /**
     * Load user by email
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );
    }
}

