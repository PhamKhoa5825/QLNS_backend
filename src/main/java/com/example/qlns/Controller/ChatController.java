package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.SendMessageRequest;
import com.example.qlns.DTO.Response.ChatRoomDTO;
import com.example.qlns.DTO.Response.MessageDTO;
import com.example.qlns.Entity.ChatRoom;
import com.example.qlns.Entity.ChatRoomMember;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.Message;
import com.example.qlns.Entity.User;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.UserRepository;
import com.example.qlns.Security.SecurityService;
import com.example.qlns.Service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// =============================================
// TV4 - ChatController
// =============================================
@RestController
@RequestMapping("/api/chat")
class ChatController {
    private final ChatService chatService;
    private final SecurityService securityService;
    private final EmployeeRepository empRepo;
    private final UserRepository userRepo;
    private final com.example.qlns.Repository.ChatRoomMemberRepository memberRepo;

    ChatController(ChatService chatService, SecurityService securityService,
                   EmployeeRepository empRepo, UserRepository userRepo,
                   com.example.qlns.Repository.ChatRoomMemberRepository memberRepo) {
        this.chatService = chatService;
        this.securityService = securityService;
        this.empRepo = empRepo;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
    }

    private ChatRoomDTO mapToDTO(ChatRoom room, Long currentUserId) {
        ChatRoomDTO dto = ChatRoomDTO.from(room);
        List<ChatRoomMember> members = memberRepo.findByRoomId(room.getId());
        
        List<String> names = members.stream()
                .map(m -> {
                    String name = m.getUser().getUsername();
                    if (m.getUser().getEmployeeId() != null) {
                        name = empRepo.findById(m.getUser().getEmployeeId())
                                .map(Employee::getFullName)
                                .orElse(m.getUser().getUsername());
                    }
                    
                    // Nếu là chat PRIVATE và đây không phải là user hiện tại, gán vào otherParticipantName
                    if ("PRIVATE".equals(room.getType().name()) && !m.getUser().getId().equals(currentUserId)) {
                        dto.setOtherParticipantName(name);
                    }
                    
                    return name;
                })
                .collect(Collectors.toList());
        
        dto.setMemberNames(names);
        return dto;
    }

    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getRooms(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getRoomsForUser(userId).stream()
                .map(room -> this.mapToDTO(room, userId)).collect(Collectors.toList()));
    }

    @PostMapping("/rooms/private")
    public ResponseEntity<ChatRoomDTO> getOrCreatePrivate(@RequestParam Long userId1,
                                                          @RequestParam Long userId2) {
        // Giả sử người gọi là userId1 (trong thực tế có thể lấy từ SecurityContext)
        return ResponseEntity.ok(mapToDTO(chatService.getOrCreatePrivateRoom(userId1, userId2), userId1));
    }

    // POST /api/chat/rooms/group — Tạo nhóm chat phòng ban
    // Body: { "departmentId": 1, "name": "Nhóm Phòng IT", "creatorUserId": 2 }
    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoomDTO> createGroupRoom(@RequestBody Map<String, Object> body) {
        Long departmentId = Long.valueOf(body.get("departmentId").toString());
        String name = (String) body.get("name");
        Long creatorUserId = Long.valueOf(body.get("creatorUserId").toString());

        // Manager chỉ tạo nhóm cho phòng ban mình
        securityService.validateManagerDepartment(departmentId);

        // Tạo phòng chat nhóm
        ChatRoom room = chatService.createDepartmentRoom(departmentId, name);

        // Tự động thêm tất cả NV trong phòng ban vào group
        List<Employee> employees = empRepo.findByDepartmentIdAndStatus(departmentId, EmployeeStatus.ACTIVE);
        for (Employee emp : employees) {
            Optional<User> user = userRepo.findByEmail(emp.getEmail());
            user.ifPresent(u -> chatService.addMember(room.getId(), u.getId()));
        }

        // Cũng thêm người tạo (nếu chưa có)
        chatService.addMember(room.getId(), creatorUserId);

        return ResponseEntity.ok(ChatRoomDTO.from(room));
    }

    private MessageDTO mapToMessageDTO(Message msg) {
        MessageDTO dto = MessageDTO.from(msg);
        if (msg.getSender() != null && msg.getSender().getEmployeeId() != null) {
            empRepo.findById(msg.getSender().getEmployeeId())
                    .ifPresent(emp -> dto.setSenderName(emp.getFullName()));
        } else if (msg.getSender() != null) {
            dto.setSenderName(msg.getSender().getUsername());
        }
        return dto;
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody SendMessageRequest req) {
        MessageType type = req.getMessageType() != null ?
                MessageType.valueOf(req.getMessageType()) : MessageType.TEXT;
        Message msg = chatService.sendMessage(req.getRoomId(), req.getSenderId(), req.getMessage(), type);
        return ResponseEntity.ok(mapToMessageDTO(msg));
    }

    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getMessages(roomId).stream()
                .map(this::mapToMessageDTO).collect(Collectors.toList()));
    }

    // Polling: Android gọi mỗi 3-5 giây để lấy tin nhắn mới
    @GetMapping("/messages/{roomId}/new")
    public ResponseEntity<List<MessageDTO>> getNewMessages(@PathVariable Long roomId,
                                                           @RequestParam Long lastMessageId) {
        return ResponseEntity.ok(chatService.getNewMessages(roomId, lastMessageId).stream()
                .map(this::mapToMessageDTO).collect(Collectors.toList()));
    }
}

