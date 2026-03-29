package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.SeenRequest;
import com.example.qlns.DTO.Request.SendMessageRequest;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Repository.UserRepository;
import com.example.qlns.Service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatWebSocketController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    // Lấy userId từ STOMP Principal (JWT đã xác thực tại WebSocketAuthInterceptor)
    private Long getUserId(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    // SEND /app/chat.send → broadcast /topic/room/{roomId}
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        MessageType type = request.getMessageType() != null
                ? MessageType.valueOf(request.getMessageType())
                : MessageType.TEXT;

        chatService.sendMessageAdvanced(
                request.getRoomId(), getUserId(principal), request.getMessage(), type,
                request.getReplyToId(),
                request.getMetadata(), null, null, null);
    }

    // SEND /app/chat.seen → broadcast /topic/room/{roomId}
    @MessageMapping("/chat.seen")
    public void markSeen(@Payload SeenRequest request, Principal principal) {
        chatService.markRoomAsSeen(getUserId(principal), request.getRoomId(), request.getLastSeenMessageId());
    }

    // SEND /app/chat.recall → broadcast /topic/room/{roomId}
    @MessageMapping("/chat.recall")
    public void recallMessage(@Payload Map<String, Long> payload, Principal principal) {
        Long messageId = payload.get("messageId");
        chatService.recallMessage(getUserId(principal), messageId);
    }

}

