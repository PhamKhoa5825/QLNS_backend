package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.ReactionRequest;
import com.example.qlns.DTO.Request.SeenRequest;
import com.example.qlns.DTO.Request.SendMessageRequest;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

// [Chat] WebSocket Controller - Xử lý các STOMP message từ client
// Endpoint prefix: /app (đã cấu hình trong WebSocketConfig)
// Tuân thủ MVC: Controller chỉ nhận request và gọi Service
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    // [Chat] Real-time - Gửi tin nhắn mới qua WebSocket
    // Client gửi: SEND /app/chat.send {roomId, message, messageType, replyToId,
    // voiceDuration, metadata}
    // Server broadcast: /topic/room/{roomId} → ChatEventDTO (NEW_MESSAGE)
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        Long senderId = request.getSenderId();
        MessageType type = request.getMessageType() != null
                ? MessageType.valueOf(request.getMessageType())
                : MessageType.TEXT;

        chatService.sendMessageAdvanced(
                request.getRoomId(), senderId, request.getMessage(), type,
                request.getReplyToId(),
                request.getMetadata(), null, null, null);
    }

    // [Chat] Đã xem - Đánh dấu đã đọc tin nhắn
    // Client gửi: SEND /app/chat.seen {userId, roomId, lastSeenMessageId}
    // Server broadcast: /topic/room/{roomId} → ChatEventDTO (SEEN)
    @MessageMapping("/chat.seen")
    public void markSeen(@Payload SeenRequest request, Principal principal) {
        chatService.markRoomAsSeen(request.getUserId(), request.getRoomId(), request.getLastSeenMessageId());
    }

    // [Chat] Thu hồi - Xóa tin nhắn gửi nhầm ở cả 2 phía
    // Client gửi: SEND /app/chat.recall {messageId}
    // Server broadcast: /topic/room/{roomId} → ChatEventDTO (RECALL)
    @MessageMapping("/chat.recall")
    public void recallMessage(@Payload java.util.Map<String, Long> payload, Principal principal) {
        Long messageId = payload.get("messageId");
        Long senderId = payload.get("senderId");
        chatService.recallMessage(senderId, messageId);
    }

    // [Chat] Reaction - Thả cảm xúc lên tin nhắn (toggle)
    // Client gửi: SEND /app/chat.reaction {userId, messageId, emoji}
    // Server broadcast: /topic/room/{roomId} → ChatEventDTO (REACTION)
    @MessageMapping("/chat.reaction")
    public void toggleReaction(@Payload ReactionRequest request, Principal principal) {
        chatService.toggleReaction(request.getUserId(), request.getMessageId(), request.getEmoji());
    }
}
