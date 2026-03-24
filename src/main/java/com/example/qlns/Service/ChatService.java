package com.example.qlns.Service;

import com.example.qlns.DTO.Response.ChatEventDTO;
import com.example.qlns.DTO.Response.ChatRoomDTO;
import com.example.qlns.DTO.Response.ContactDTO;
import com.example.qlns.DTO.Response.MessageDTO;
import com.example.qlns.DTO.Response.ReactionDTO;
import com.example.qlns.DTO.Response.ReadReceiptDTO;
import com.example.qlns.Entity.*;
import com.example.qlns.Enum.ChatEventType;
import com.example.qlns.Enum.ChatRoomType;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Exception.ForbiddenException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// [Chat] Service chính xử lý toàn bộ logic nhắn tin
// Tuân thủ SOLID: Single Responsibility cho messaging, Open/Closed cho mở rộng message types
@Service
public class ChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatRoomMemberRepository memberRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final MessageReadReceiptRepository readReceiptRepo;
    private final MessageReactionRepository reactionRepo;
    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository departmentRepo;
    private final SimpMessagingTemplate messagingTemplate;

    // [Chat] Constructor injection - tuân thủ Dependency Inversion
    public ChatService(ChatRoomRepository roomRepo,
            ChatRoomMemberRepository memberRepo,
            MessageRepository messageRepo,
            UserRepository userRepo,
            MessageReadReceiptRepository readReceiptRepo,
            MessageReactionRepository reactionRepo,
            EmployeeRepository employeeRepo,
            DepartmentRepository departmentRepo,
            SimpMessagingTemplate messagingTemplate) {
        this.roomRepo = roomRepo;
        this.memberRepo = memberRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.readReceiptRepo = readReceiptRepo;
        this.reactionRepo = reactionRepo;
        this.employeeRepo = employeeRepo;
        this.departmentRepo = departmentRepo;
        this.messagingTemplate = messagingTemplate;
    }

    // [Chat] Tự động tạo phòng chat cho các phòng ban chưa có nhóm khi app khởi
    // động
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncDepartmentRoomsOnStartup() {
        departmentRepo.findAll().forEach(dept -> {
            if (roomRepo.findByDepartmentId(dept.getId()).isEmpty()) {
                initDepartmentRoom(dept);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] PHÒNG CHAT - Quản lý room
    // ══════════════════════════════════════════════════════════════

    // [Chat] Lấy danh sách phòng chat kèm metadata (unreadCount, lastMessage) cho
    // Frontend
    //
    public List<ChatRoomDTO> getRoomsWithMetadata(Long userId) {
        List<ChatRoomMember> memberships = memberRepo.findByUserId(userId);

        return memberships.stream().map(membership -> {
            ChatRoom room = membership.getRoom();
            ChatRoomDTO dto = ChatRoomDTO.from(room);

            // [Chat] Tính số tin chưa đọc dựa trên lastReadMessageId
            Long lastReadId = membership.getLastReadMessageId();
            if (lastReadId != null) {
                dto.setUnreadCount(messageRepo.countByRoomIdAndIdGreaterThan(room.getId(), lastReadId));
            } else {
                // Chưa đọc bao giờ → đếm tất cả tin nhắn
                dto.setUnreadCount(messageRepo.countByRoomIdAndIdGreaterThan(room.getId(), 0L));
            }

            // [Chat] Lấy tin nhắn mới nhất để hiển thị preview
            List<Message> messages = messageRepo.findByRoomIdOrderByCreatedAtAsc(room.getId());
            if (!messages.isEmpty()) {
                Message last = messages.get(messages.size() - 1);
                dto.setLastMessage(last.getIsRecalled() ? "Tin nhắn đã bị thu hồi" : last.getMessage());
                dto.setLastMessageTime(last.getCreatedAt() != null ? last.getCreatedAt().toString() : null);
            }

            return dto;
        }).toList();
    }

    // [Chat] Tạo hoặc lấy phòng chat riêng 1-1 giữa 2 user
    @Transactional
    public ChatRoom getOrCreatePrivateRoom(Long userId1, Long userId2) {
        return roomRepo.findPrivateRoom(userId1, userId2).orElseGet(() -> {
            User creator = userRepo.findById(userId1)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
            ChatRoom room = new ChatRoom();
            room.setType(ChatRoomType.PRIVATE);
            room.setCreatedBy(creator);
            ChatRoom saved = roomRepo.save(room);
            addMember(saved.getId(), userId1);
            addMember(saved.getId(), userId2);
            return saved;
        });
    }

    // [Chat] Khởi tạo phòng chat mặc định cho phòng ban
    // - Manager của phòng ban → ADMIN
    // - Toàn bộ nhân viên còn lại → MEMBER
    // - Nếu phòng đã tồn tại → cập nhật thành viên
    @Transactional
    public ChatRoom initDepartmentRoom(Department department) {
        // [Chat] Tìm hoặc tạo mới phòng DEPARTMENT theo departmentId
        ChatRoom room = roomRepo.findByDepartmentId(department.getId()).orElseGet(() -> {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setName(department.getName());
            newRoom.setType(ChatRoomType.DEPARTMENT);
            newRoom.setDepartment(department);
            return roomRepo.save(newRoom);
        });

        // [Chat] Xác định manager của phòng
        Employee manager = department.getManager();

        // [Chat] Thêm manager làm ADMIN (nếu có User tài khoản)
        if (manager != null) {
            userRepo.findByEmployeeId(manager.getId()).ifPresent(managerUser -> {
                // Nếu chưa có createdBy → gán creator là manager
                if (room.getCreatedBy() == null) {
                    room.setCreatedBy(managerUser);
                    roomRepo.save(room);
                }
                addMemberWithRole(room.getId(), managerUser.getId(), "ADMIN");
            });
        }

        // [Chat] Thêm tất cả nhân viên ACTIVE trong phòng làm MEMBER
        List<Employee> employees = employeeRepo.findByDepartmentIdAndStatus(
                department.getId(), com.example.qlns.Enum.EmployeeStatus.ACTIVE);
        for (Employee emp : employees) {
            // Bỏ qua manager (đã thêm ADMIN ở trên)
            if (manager != null && emp.getId().equals(manager.getId()))
                continue;
            userRepo.findByEmployeeId(emp.getId())
                    .ifPresent(user -> addMemberWithRole(room.getId(), user.getId(), "MEMBER"));
        }

        // [Chat] Gửi tin hệ thống nếu phòng mới được tạo
        if (room.getCreatedBy() != null && messageRepo.findByRoomIdOrderByCreatedAtAsc(room.getId()).isEmpty()) {
            sendSystemMessage(room.getId(), "Phòng chat \"" + department.getName() + "\" đã được tạo");
        }

        return room;
    }

    @Transactional
    public ChatRoom createGroupRoom(String name, List<Long> memberUserIds, Long creatorUserId) {
        User creator = userRepo.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        ChatRoom room = new ChatRoom();
        room.setName(name);
        room.setType(ChatRoomType.GROUP);
        room.setCreatedBy(creator);
        ChatRoom saved = roomRepo.save(room);

        // [Chat] Thêm creator với role ADMIN
        addMemberWithRole(saved.getId(), creatorUserId, "ADMIN");

        // [Chat] Thêm các thành viên ban đầu
        if (memberUserIds != null) {
            for (Long uid : memberUserIds) {
                if (!uid.equals(creatorUserId)) {
                    addMemberWithRole(saved.getId(), uid, "MEMBER");
                }
            }
        }

        // [Chat] Gửi tin nhắn hệ thống thông báo tạo nhóm
        String sysMsg = creator.getUsername() + " đã tạo nhóm \"" + name + "\"";
        sendSystemMessage(saved.getId(), sysMsg);
        return saved;
    }

    // [Chat] Đổi tên phòng chat nhóm
    @Transactional
    public ChatRoom updateRoomName(Long roomId, String newName, Long updatedByUserId) {
        ChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng chat"));
        User updater = userRepo.findById(updatedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!memberRepo.existsByRoomIdAndUserId(roomId, updatedByUserId)) {
            throw new ForbiddenException("Bạn không trong phòng chat này");
        }

        String oldName = room.getName();
        room.setName(newName);
        ChatRoom saved = roomRepo.save(room);

        // [Chat] Tin nhắn hệ thống thông báo đổi tên
        String sysMsg = updater.getUsername() + " đã đổi tên nhóm từ \"" + oldName + "\" thành \"" + newName + "\"";
        sendSystemMessage(roomId, sysMsg);
        return saved;
    }

    // [Chat] Thêm nhiều thành viên vào nhóm (Manager dùng)
    @Transactional
    public void addMembers(Long roomId, List<Long> userIds, Long addedByUserId) {
        User addedBy = userRepo.findById(addedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!memberRepo.existsByRoomIdAndUserId(roomId, addedByUserId)) {
            throw new ForbiddenException("Bạn không trong phòng chat này");
        }

        for (Long uid : userIds) {
            if (!memberRepo.existsByRoomIdAndUserId(roomId, uid)) {
                addMemberWithRole(roomId, uid, "MEMBER");
                // [Chat] Tin nhắn hệ thống cho mỗi thành viên được thêm
                userRepo.findById(uid).ifPresent(newUser -> {
                    String sysMsg = addedBy.getUsername() + " đã thêm " + newUser.getUsername() + " vào nhóm";
                    sendSystemMessage(roomId, sysMsg);
                });
            }
        }
    }

    // [Chat] Xóa thành viên khỏi nhóm
    @Transactional
    public void removeMemberFromRoom(Long roomId, Long userId, Long removedByUserId) {
        User removedBy = userRepo.findById(removedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User target = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String sysMsg;
        if (userId.equals(removedByUserId)) {
            sysMsg = target.getUsername() + " đã rời khỏi nhóm";
        } else {
            sysMsg = removedBy.getUsername() + " đã xóa " + target.getUsername() + " khỏi nhóm";
        }

        memberRepo.deleteByRoomIdAndUserId(roomId, userId);
        sendSystemMessage(roomId, sysMsg);
    }

    // [Chat] Lấy danh sách thành viên trong phòng chat
    public List<ChatRoomMember> getRoomMembers(Long roomId) {
        return memberRepo.findByRoomId(roomId);
    }

    // [Chat] Tìm kiếm tin nhắn theo keyword trong phòng chat
    public List<Message> searchMessages(Long roomId, String keyword) {
        return messageRepo.searchMessages(roomId, keyword);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────

    // [Chat] Thêm thành viên với role cụ thể
    private void addMemberWithRole(Long roomId, Long userId, String role) {
        if (!memberRepo.existsByRoomIdAndUserId(roomId, userId)) {
            ChatRoomMember member = new ChatRoomMember();
            roomRepo.findById(roomId).ifPresent(member::setRoom);
            userRepo.findById(userId).ifPresent(member::setUser);
            member.setRole(role);
            memberRepo.save(member);
        }
    }

    // [Chat] Gửi tin nhắn hệ thống vào phòng (dùng sender = creator của room hoặc
    // bất kỳ admin)
    private void sendSystemMessage(Long roomId, String content) {
        ChatRoom room = roomRepo.findById(roomId).orElse(null);
        if (room == null)
            return;

        // [Chat] Lấy creator làm sender ảo cho tin nhắn hệ thống
        User systemUser = room.getCreatedBy();
        if (systemUser == null)
            return;

        Message sys = new Message();
        sys.setRoom(room);
        sys.setSender(systemUser);
        sys.setMessage(content);
        sys.setMessageType(MessageType.SYSTEM);
        Message saved = messageRepo.save(sys);

        broadcastEvent(roomId, ChatEventType.NEW_MESSAGE, MessageDTO.from(saved), null, systemUser.getId());
    }

    // [Chat] Thêm thành viên vào phòng chat
    public void addMember(Long roomId, Long userId) {
        addMemberWithRole(roomId, userId, "MEMBER");
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] GỬI TIN NHẮN - Core messaging qua WebSocket
    // ══════════════════════════════════════════════════════════════

    // [Chat] Gửi tin nhắn nâng cao - Hỗ trợ reply, metadata, file
    @Transactional
    public Message sendMessageAdvanced(Long roomId, Long senderId, String content,
            MessageType type, Long replyToId,
            String metadata,
            String fileUrl, String fileName, Long fileSize) {
        ChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng chat"));
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!memberRepo.existsByRoomIdAndUserId(roomId, senderId)) {
            throw new ForbiddenException("Bạn không trong phòng chat này");
        }

        Message msg = new Message();
        msg.setRoom(room);
        msg.setSender(sender);
        msg.setMessage(content);
        msg.setMessageType(type);
        msg.setReplyToId(replyToId);
        msg.setMetadata(metadata);
        msg.setFileUrl(fileUrl);
        msg.setFileName(fileName);
        msg.setFileSize(fileSize);
        Message saved = messageRepo.save(msg);

        // [Chat] Tạo DTO kèm thông tin reply nếu có
        MessageDTO dto = MessageDTO.from(saved);
        if (replyToId != null) {
            messageRepo.findById(replyToId).ifPresent(reply -> dto.setReplyToMessage(MessageDTO.from(reply)));
        }

        // [Chat] Broadcast qua WebSocket
        broadcastEvent(roomId, ChatEventType.NEW_MESSAGE, dto, null, senderId);
        return saved;
    }

    // [Chat] Lấy tin nhắn trong phòng
    public List<Message> getMessages(Long roomId) {
        return messageRepo.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] THU HỒI - Xóa tin nhắn gửi nhầm ở cả 2 phía
    // ══════════════════════════════════════════════════════════════

    // [Chat] Thu hồi tin nhắn - Chỉ người gửi mới được thu hồi
    @Transactional
    public Message recallMessage(Long senderId, Long messageId) {
        Message msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin nhắn"));

        // [Chat] Kiểm tra quyền: chỉ người gửi mới được thu hồi
        if (!msg.getSender().getId().equals(senderId)) {
            throw new ForbiddenException("Chỉ người gửi mới có thể thu hồi tin nhắn");
        }

        msg.setIsRecalled(true);
        msg.setMessage("Tin nhắn đã bị thu hồi");
        Message saved = messageRepo.save(msg);

        // [Chat] Broadcast event thu hồi để client cập nhật UI
        broadcastEvent(msg.getRoom().getId(), ChatEventType.RECALL, MessageDTO.from(saved), null, senderId);
        return saved;
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] REACTION - Thả cảm xúc lên tin nhắn
    // ══════════════════════════════════════════════════════════════

    // [Chat] Thêm/Toggle reaction trên tin nhắn
    @Transactional
    public void toggleReaction(Long userId, Long messageId, String emoji) {
        Message msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin nhắn"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // [Chat] Toggle: nếu đã react emoji này thì xóa, chưa thì thêm
        Optional<MessageReaction> existing = reactionRepo.findByMessageIdAndUserIdAndEmoji(messageId, userId, emoji);
        if (existing.isPresent()) {
            reactionRepo.delete(existing.get());
        } else {
            MessageReaction reaction = new MessageReaction();
            reaction.setMessage(msg);
            reaction.setUser(user);
            reaction.setEmoji(emoji);
            reactionRepo.save(reaction);
        }

        // [Chat] Broadcast cập nhật reactions
        List<ReactionDTO> reactions = getReactionsForMessage(messageId);
        broadcastEvent(msg.getRoom().getId(), ChatEventType.REACTION, MessageDTO.from(msg), reactions, userId);
    }

    // [Chat] Lấy reactions tổng hợp theo emoji cho 1 tin nhắn
    public List<ReactionDTO> getReactionsForMessage(Long messageId) {
        List<MessageReaction> allReactions = reactionRepo.findByMessageId(messageId);

        // [Chat] Nhóm reactions theo emoji → tạo ReactionDTO
        Map<String, List<MessageReaction>> grouped = allReactions.stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji));

        return grouped.entrySet().stream().map(entry -> {
            List<Long> userIds = entry.getValue().stream().map(r -> r.getUser().getId()).toList();
            List<String> userNames = entry.getValue().stream().map(r -> r.getUser().getUsername()).toList();
            return new ReactionDTO(entry.getKey(), entry.getValue().size(), userIds, userNames);
        }).toList();
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] ĐÃ XEM (SEEN) - Hiển thị danh sách người đã xem
    // ══════════════════════════════════════════════════════════════

    // [Chat] Đánh dấu đã xem tin nhắn
    @Transactional
    public void markAsSeen(Long userId, Long messageId) {
        if (readReceiptRepo.existsByMessageIdAndUserId(messageId, userId)) {
            return; // Đã xem rồi, không cần cập nhật
        }

        Message msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin nhắn"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        MessageReadReceipt receipt = new MessageReadReceipt();
        receipt.setMessage(msg);
        receipt.setUser(user);
        receipt.setSeenAt(LocalDateTime.now());
        readReceiptRepo.save(receipt);

        // [Chat] Cập nhật lastReadMessageId trong ChatRoomMember
        memberRepo.findByRoomIdAndUserId(msg.getRoom().getId(), userId)
                .ifPresent(member -> {
                    member.setLastReadMessageId(messageId);
                    memberRepo.save(member);
                });

        // [Chat] Broadcast danh sách người đã xem
        List<ReadReceiptDTO> seenList = readReceiptRepo.findByMessageId(messageId).stream()
                .map(ReadReceiptDTO::from).toList();
        broadcastEvent(msg.getRoom().getId(), ChatEventType.SEEN, null, seenList, userId);
    }

    // [Chat] Đánh dấu đã xem tất cả tin nhắn đến lastSeenMessageId
    @Transactional
    public void markRoomAsSeen(Long userId, Long roomId, Long lastSeenMessageId) {
        memberRepo.findByRoomIdAndUserId(roomId, userId)
                .ifPresent(member -> {
                    member.setLastReadMessageId(lastSeenMessageId);
                    memberRepo.save(member);
                });

        // [Chat] Tạo read receipt cho tin nhắn cuối cùng
        markAsSeen(userId, lastSeenMessageId);
    }

    // ══════════════════════════════════════════════════════════════
    // [Chat] DANH BẠ THÔNG MINH - Tra cứu đồng nghiệp
    // ══════════════════════════════════════════════════════════════

    // [Chat] Tìm kiếm đồng nghiệp theo keyword, kỹ năng, chức vụ, trạng thái
    public List<ContactDTO> searchContacts(String keyword, String skill, String position, String status) {
        List<Employee> employees = employeeRepo.findAll();

        return employees.stream()
                // [Chat] Lọc theo keyword (tên, email)
                .filter(emp -> keyword == null || keyword.isEmpty()
                        || emp.getFullName().toLowerCase().contains(keyword.toLowerCase())
                        || emp.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                // [Chat] Lọc theo kỹ năng
                .filter(emp -> skill == null || skill.isEmpty()
                        || (emp.getSkills() != null && emp.getSkills().toLowerCase().contains(skill.toLowerCase())))
                // [Chat] Lọc theo chức vụ
                .filter(emp -> position == null || position.isEmpty()
                        || emp.getPosition().toLowerCase().contains(position.toLowerCase()))
                // [Chat] Lọc theo trạng thái nhân viên
                .filter(emp -> status == null || status.isEmpty()
                        || emp.getStatus().name().equalsIgnoreCase(status))
                .map(emp -> {
                    // [Chat] Tìm userId tương ứng với employee
                    Long userId = userRepo.findByEmployeeId(emp.getId())
                            .map(User::getId).orElse(null);
                    String userStatus = emp.getStatus().name();
                    return ContactDTO.from(emp, userId, userStatus);
                })
                .toList();
    }

    // [Chat] Gửi event qua WebSocket tới tất cả thành viên trong phòng
    private void broadcastEvent(Long roomId, ChatEventType eventType,
            MessageDTO message, Object data, Long triggeredBy) {
        ChatEventDTO event = ChatEventDTO.of(eventType.name(), roomId, message, data, triggeredBy);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

}
