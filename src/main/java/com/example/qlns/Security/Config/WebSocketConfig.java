package com.example.qlns.Security.Config;

import com.example.qlns.Security.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// [Chat] Cấu hình WebSocket + STOMP cho real-time messaging
// - Endpoint kết nối: /ws
// - Client gửi message tới: /app/...
// - Client subscribe tại: /topic/... và /user/.../queue/...
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    // [Chat] Cấu hình message broker
    // - /app: prefix cho các message từ client gửi lên server (@MessageMapping)
    // - /topic: prefix cho broadcast tới nhiều subscriber (phòng chat)
    // - /user: prefix cho message riêng tới 1 user (bot, notification)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    // [Chat] Đăng ký STOMP endpoint cho client kết nối WebSocket
    // - /ws: endpoint chính, hỗ trợ SockJS fallback cho browser cũ
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // [Chat] SockJS endpoint cho browser
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        // [Chat] Raw WebSocket endpoint cho Android (không SockJS)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    // [Chat] Đăng ký interceptor xác thực JWT trên channel inbound
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
