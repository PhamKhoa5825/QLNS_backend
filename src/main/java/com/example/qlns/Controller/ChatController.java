package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.SendMessageRequest;
import com.example.qlns.DTO.Response.ChatRoomDTO;
import com.example.qlns.DTO.Response.MessageDTO;
import com.example.qlns.Entity.Message;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV4 - ChatController
// =============================================
@RestController
@RequestMapping("/api/chat")
class ChatController {
    private final ChatService chatService;

    ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getRooms(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getRoomsForUser(userId).stream()
                .map(ChatRoomDTO::from).collect(Collectors.toList()));
    }

    @PostMapping("/rooms/private")
    public ResponseEntity<ChatRoomDTO> getOrCreatePrivate(@RequestParam Long userId1,
                                                          @RequestParam Long userId2) {
        return ResponseEntity.ok(ChatRoomDTO.from(chatService.getOrCreatePrivateRoom(userId1, userId2)));
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody SendMessageRequest req) {
        MessageType type = req.getMessageType() != null ?
                MessageType.valueOf(req.getMessageType()) : MessageType.TEXT;
        Message msg = chatService.sendMessage(req.getRoomId(), req.getSenderId(), req.getMessage(), type);
        return ResponseEntity.ok(MessageDTO.from(msg));
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId).stream()
                .map(MessageDTO::from).collect(Collectors.toList()));
    }

    // Polling: Android gọi mỗi 3-5 giây để lấy tin nhắn mới
    @GetMapping("/messages/{roomId}/new")
    public ResponseEntity<List<MessageDTO>> getNewMessages(@PathVariable Long roomId,
                                                           @RequestParam Long lastMessageId) {
        return ResponseEntity.ok(chatService.getNewMessages(roomId, lastMessageId).stream()
                .map(MessageDTO::from).collect(Collectors.toList()));
    }
}
