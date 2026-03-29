package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.AddMembersRequest;
import com.example.qlns.DTO.Request.CreateGroupRoomRequest;
import com.example.qlns.DTO.Request.SendMessageRequest;
import com.example.qlns.DTO.Request.UpdateRoomNameRequest;
import com.example.qlns.DTO.Response.ChatRoomDTO;
import com.example.qlns.DTO.Response.ContactDTO;
import com.example.qlns.DTO.Response.MessageDTO;
import com.example.qlns.DTO.Response.RoomMemberDTO;
import com.example.qlns.Entity.Message;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Repository.UserRepository;
import com.example.qlns.Service.ChatService;
import com.example.qlns.Service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService,
            FileStorageService fileStorageService,
            UserRepository userRepository) {
        this.chatService = chatService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    // Lấy userId từ JWT token trong SecurityContext
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    // ══════════════════════════════════════════════════════════════
    // PHÒNG CHAT
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/rooms/me")
    public ResponseEntity<List<ChatRoomDTO>> getRooms() {
        return ResponseEntity.ok(chatService.getRoomsWithMetadata(getCurrentUserId()));
    }

    @PostMapping("/rooms/private")
    public ResponseEntity<ChatRoomDTO> getOrCreatePrivate(@RequestParam Long targetUserId) {
        return ResponseEntity.ok(ChatRoomDTO.from(
                chatService.getOrCreatePrivateRoom(getCurrentUserId(), targetUserId)));
    }

    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoomDTO> createGroupRoom(@RequestBody CreateGroupRoomRequest req) {
        return ResponseEntity.ok(ChatRoomDTO.from(
                chatService.createGroupRoom(req.getName(), req.getMemberUserIds(), getCurrentUserId())));
    }

    @PutMapping("/rooms/{roomId}/name")
    public ResponseEntity<ChatRoomDTO> updateRoomName(@PathVariable Long roomId,
            @RequestBody UpdateRoomNameRequest req) {
        return ResponseEntity.ok(ChatRoomDTO.from(
                chatService.updateRoomName(roomId, req.getName(), getCurrentUserId())));
    }

    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<Void> addMembers(@PathVariable Long roomId,
            @RequestBody AddMembersRequest req) {
        chatService.addMembers(roomId, req.getMemberUserIds(), getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    // Nếu userId == currentUserId thì tự rời, ngược lại là bị kick bởi admin
    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long roomId,
            @PathVariable Long userId) {
        chatService.removeMemberFromRoom(roomId, userId, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}/members")
    public ResponseEntity<List<RoomMemberDTO>> getRoomMembers(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getRoomMembersDTO(roomId));
    }

    // ══════════════════════════════════════════════════════════════
    // TIN NHẮN
    // ══════════════════════════════════════════════════════════════

    // Fallback khi WebSocket không khả dụng
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody SendMessageRequest req) {
        MessageType type = req.getMessageType() != null
                ? MessageType.valueOf(req.getMessageType())
                : MessageType.TEXT;
        Message msg = chatService.sendMessageAdvanced(
                req.getRoomId(), getCurrentUserId(), req.getMessage(), type,
                req.getReplyToId(), req.getMetadata(), null, null, null);
        return ResponseEntity.ok(MessageDTO.from(msg));
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId).stream()
                .map(msg -> {
                    MessageDTO dto = MessageDTO.from(msg);
                    dto.setSenderName(msg.getSender() != null
                            ? chatService.resolveDisplayName(msg.getSender().getId())
                            : null);
                    if (msg.getReplyToId() != null) {
                        chatService.findMessageById(msg.getReplyToId())
                                .ifPresent(reply -> {
                                    MessageDTO replyDto = MessageDTO.from(reply);
                                    replyDto.setSenderName(reply.getSender() != null
                                            ? chatService.resolveDisplayName(reply.getSender().getId())
                                            : null);
                                    dto.setReplyToMessage(replyDto);
                                });
                    }
                    return dto;
                }).collect(Collectors.toList()));
    }

    @GetMapping("/messages/{roomId}/search")
    public ResponseEntity<List<MessageDTO>> searchMessages(@PathVariable Long roomId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(chatService.searchMessages(roomId, keyword).stream()
                .map(msg -> {
                    MessageDTO dto = MessageDTO.from(msg);
                    dto.setSenderName(msg.getSender() != null
                            ? chatService.resolveDisplayName(msg.getSender().getId())
                            : null);
                    return dto;
                }).collect(Collectors.toList()));
    }

    // Gọi khi client mở màn hình chat để reset unreadCount
    @PostMapping("/rooms/{roomId}/seen")
    public ResponseEntity<Void> markRoomSeen(@PathVariable Long roomId,
            @RequestParam Long lastSeenMessageId) {
        chatService.markRoomAsSeen(getCurrentUserId(), roomId, lastSeenMessageId);
        return ResponseEntity.ok().build();
    }

    // ══════════════════════════════════════════════════════════════
    // ĐA PHƯƠNG TIỆN
    // ══════════════════════════════════════════════════════════════

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam Long roomId,
            @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "FILE") String messageType) {
        try {
            String fileUrl = fileStorageService.storeFile(file, roomId);
            String fileName = file.getOriginalFilename();
            Long fileSize = file.getSize();
            MessageType type = MessageType.valueOf(messageType);
            Message msg = chatService.sendMessageAdvanced(
                    roomId, getCurrentUserId(), fileName != null ? fileName : "File",
                    type, null, null, fileUrl, fileName, fileSize);
            return ResponseEntity.ok(MessageDTO.from(msg));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("messageType không hợp lệ: " + messageType);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload thất bại: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // THU HỒI
    // ══════════════════════════════════════════════════════════════

    @PutMapping("/messages/{messageId}/recall")
    public ResponseEntity<MessageDTO> recallMessage(@PathVariable Long messageId) {
        Message msg = chatService.recallMessage(getCurrentUserId(), messageId);
        return ResponseEntity.ok(MessageDTO.from(msg));
    }

    // ══════════════════════════════════════════════════════════════
    // DANH BẠ
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDTO>> searchContacts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long roomId) {
        return ResponseEntity
                .ok(chatService.searchContacts(getCurrentUserId(), keyword, skill, position, status, roomId));
    }
}
