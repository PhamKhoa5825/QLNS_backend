package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.SendMessageRequest;
import com.example.qlns.DTO.Response.ChatRoomDTO;
import com.example.qlns.DTO.Response.ContactDTO;
import com.example.qlns.DTO.Response.MessageDTO;
import com.example.qlns.Entity.Message;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Service.ChatService;
import com.example.qlns.Service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

// [Chat] REST Controller - Xử lý các HTTP request cho chat
// Tuân thủ MVC: Controller chỉ nhận request, validate, gọi Service, trả response
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final FileStorageService fileStorageService;

    // [Chat] Constructor injection - tuân thủ Dependency Inversion
    public ChatController(ChatService chatService,
                          FileStorageService fileStorageService) {
        this.chatService = chatService;
        this.fileStorageService = fileStorageService;
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] PHÒNG CHAT - REST endpoints quản lý room
    // ══════════════════════════════════════════════════════════════

    // [Chat] Lấy danh sách phòng chat của user (kèm unreadCount, lastMessage, avatarUrl)
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getRooms(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getRoomsWithMetadata(userId));
    }

    // [Chat] Tạo hoặc lấy phòng chat 1-1
    @PostMapping("/rooms/private")
    public ResponseEntity<ChatRoomDTO> getOrCreatePrivate(@RequestParam Long userId1,
                                                          @RequestParam Long userId2) {
        return ResponseEntity.ok(ChatRoomDTO.from(chatService.getOrCreatePrivateRoom(userId1, userId2)));
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] TIN NHẮN - REST endpoints (fallback khi WebSocket không khả dụng)
    // ══════════════════════════════════════════════════════════════

    // [Chat] Gửi tin nhắn qua REST (backup cho WebSocket)
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody SendMessageRequest req) {
        MessageType type = req.getMessageType() != null
                ? MessageType.valueOf(req.getMessageType()) : MessageType.TEXT;
        Message msg = chatService.sendMessageAdvanced(
                req.getRoomId(), req.getSenderId(), req.getMessage(), type,
                req.getReplyToId(), req.getMetadata(), null, null, null
        );
        return ResponseEntity.ok(MessageDTO.from(msg));
    }

    // [Chat] Lấy toàn bộ tin nhắn trong phòng
    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId).stream()
                .map(MessageDTO::from).collect(Collectors.toList()));
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] ĐA PHƯƠNG TIỆN - Upload file (ảnh, PDF, Excel)
    // ══════════════════════════════════════════════════════════════

    // [Chat] Upload file và gửi tin nhắn media vào phòng chat
    @PostMapping("/upload")
    public ResponseEntity<MessageDTO> uploadFile(@RequestParam Long roomId,
                                                  @RequestParam Long senderId,
                                                  @RequestParam MultipartFile file,
                                                  @RequestParam(defaultValue = "FILE") String messageType) {
        String fileUrl = fileStorageService.storeFile(file, roomId);
        String fileName = file.getOriginalFilename();
        Long fileSize = file.getSize();
        MessageType type = MessageType.valueOf(messageType);
        Message msg = chatService.sendMessageAdvanced(
                roomId, senderId, fileName != null ? fileName : "File",
                type, null, null, fileUrl, fileName, fileSize
        );
        return ResponseEntity.ok(MessageDTO.from(msg));
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] THU HỒI - REST endpoint
    // ══════════════════════════════════════════════════════════════

    // [Chat] Thu hồi tin nhắn qua REST
    @PutMapping("/messages/{messageId}/recall")
    public ResponseEntity<MessageDTO> recallMessage(@PathVariable Long messageId,
                                                     @RequestParam Long senderId) {
        Message msg = chatService.recallMessage(senderId, messageId);
        return ResponseEntity.ok(MessageDTO.from(msg));
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] REACTION - Thả cảm xúc qua REST
    // ══════════════════════════════════════════════════════════════

    // [Chat] Toggle reaction (thêm/xóa cảm xúc) qua REST
    @PostMapping("/messages/{messageId}/reaction")
    public ResponseEntity<Void> toggleReaction(@PathVariable Long messageId,
                                                @RequestParam Long userId,
                                                @RequestParam String emoji) {
        chatService.toggleReaction(userId, messageId, emoji);
        return ResponseEntity.ok().build();
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] ĐÃ XEM - Đánh dấu đã đọc
    // ══════════════════════════════════════════════════════════════

    // [Chat] Đánh dấu đã xem tin nhắn qua REST (backup cho WebSocket)
    @PostMapping("/messages/{messageId}/seen")
    public ResponseEntity<Void> markSeen(@PathVariable Long messageId,
                                          @RequestParam Long userId) {
        chatService.markAsSeen(userId, messageId);
        return ResponseEntity.ok().build();
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] DANH BẠ THÔNG MINH - Tra cứu đồng nghiệp
    // ══════════════════════════════════════════════════════════════

    // [Chat] Tìm kiếm đồng nghiệp theo keyword, kỹ năng, chức vụ, trạng thái
    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDTO>> searchContacts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(chatService.searchContacts(keyword, skill, position, status));
    }
}
