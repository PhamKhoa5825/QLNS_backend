package com.example.qlns.Security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// [Chat] WebSocket Auth Interceptor - Xác thực JWT khi client CONNECT qua STOMP
// Đọc JWT token từ STOMP header "Authorization", verify và set Principal cho session
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtService jwtService,
            CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // [Chat] Interceptor pre-send - Xác thực JWT trước khi cho phép STOMP CONNECT
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Lấy JWT token từ STOMP header "Authorization"
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // Xác thực JWT token
                    String username = jwtService.extractUsername(token);

                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtService.validateToken(token, userDetails)) {
                            // Set authentication cho WebSocket session
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(auth);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                } catch (Exception e) {
                    // Token không hợp lệ → từ chối kết nối
                    // Log lỗi nhưng không block để SockJS fallback hoạt động
                }
            }
        }

        return message;
    }
}
