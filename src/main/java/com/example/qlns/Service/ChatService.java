package com.example.qlns.Service;

import com.example.qlns.DTO.Response.ChatEventDTO;
import com.example.qlns.DTO.Response.ChatRoomDTO;
import com.example.qlns.DTO.Response.ContactDTO;
import com.example.qlns.DTO.Response.MessageDTO;

import com.example.qlns.DTO.Response.RoomMemberDTO;
import com.example.qlns.Entity.*;
import com.example.qlns.Enum.ChatEventType;
import com.example.qlns.Enum.ChatRoomType;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Enum.Role;
import com.example.qlns.Exception.ForbiddenException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatRoomRepository roomRepo;
    private final ChatRoomMemberRepository memberRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;


    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository departmentRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatRoomRepository roomRepo,
            ChatRoomMemberRepository memberRepo,
            MessageRepository messageRepo,
            UserRepository userRepo,
            EmployeeRepository employeeRepo,
            DepartmentRepository departmentRepo,
            SimpMessagingTemplate messagingTemplate) {
        this.roomRepo = roomRepo;
        this.memberRepo = memberRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.employeeRepo = employeeRepo;
        this.departmentRepo = departmentRepo;
        this.messagingTemplate = messagingTemplate;
    }

    // Khi app khởi động, tạo phòng chat cho các phòng ban chưa có
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
    // PHÒNG CHAT
    // ══════════════════════════════════════════════════════════════

    // Lấy danh sách phòng chat kèm số tin chưa đọc và tin nhắn mới nhất
    public List<ChatRoomDTO> getRoomsWithMetadata(Long userId) {
        List<ChatRoomMember> memberships = memberRepo.findByUserId(userId);

        List<ChatRoomDTO> rooms = memberships.stream().map(membership -> {
            ChatRoom room = membership.getRoom();
            ChatRoomDTO dto = ChatRoomDTO.from(room);

            if (room.getCreatedBy() != null) {
                dto.setCreatedByName(getDisplayName(room.getCreatedBy()));
            }

            // Đếm số tin chưa đọc dựa vào lastReadMessageId
            Long lastReadId = membership.getLastReadMessageId();
            if (lastReadId != null) {
                dto.setUnreadCount(messageRepo.countByRoomIdAndIdGreaterThan(room.getId(), lastReadId));
            } else {
                dto.setUnreadCount(messageRepo.countByRoomIdAndIdGreaterThan(room.getId(), 0L));
            }

            // Lấy tin nhắn mới nhất để hiển thị preview
            List<Message> messages = messageRepo.findByRoomIdOrderByCreatedAtAsc(room.getId());
            if (!messages.isEmpty()) {
                Message last = messages.get(messages.size() - 1);
                dto.setLastMessage(Boolean.TRUE.equals(last.getIsRecalled()) ? "Tin nhắn đã bị thu hồi"
                        : (last.getSender() != null
                                ? getDisplayName(last.getSender()) + ": " + last.getMessage()
                                : last.getMessage()));
                dto.setLastMessageTime(last.getCreatedAt() != null
                        ? last.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : null);
            }

            // Với phòng 1-1: lấy tên và id của người còn lại
            if (room.getType() == ChatRoomType.PRIVATE) {
                memberRepo.findByRoomId(room.getId()).stream()
                        .filter(m -> !m.getUser().getId().equals(userId))
                        .findFirst()
                        .ifPresent(other -> {
                            dto.setOtherParticipantId(other.getUser().getId());
                            Long empId = other.getUser().getEmployeeId();
                            String fullName = empId != null
                                    ? employeeRepo.findById(empId)
                                            .map(emp -> emp.getFullName())
                                            .orElse(other.getUser().getUsername())
                                    : other.getUser().getUsername();
                            dto.setOtherParticipantName(fullName);
                        });
            }

            return dto;
        }).collect(Collectors.toList());

        // Sắp xếp theo tin nhắn mới nhất lên trên
        rooms.sort((a, b) -> {
            String timeA = a.getLastMessageTime();
            String timeB = b.getLastMessageTime();
            if (timeA == null && timeB == null)
                return 0;
            if (timeA == null)
                return 1;
            if (timeB == null)
                return -1;
            return timeB.compareTo(timeA); // DESC
        });

        return rooms;
    }

    // Tạo phòng chat 1-1 nếu chưa có, hoặc trả về phòng đã tồn tại
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

    // Khởi tạo hoặc đồng bộ phòng chat của một phòng ban:
    // manager → ADMIN, các nhân viên còn lại → MEMBER
    @Transactional
    public ChatRoom initDepartmentRoom(Department department) {
        ChatRoom room = roomRepo.findByDepartmentId(department.getId()).orElseGet(() -> {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setName(department.getName());
            newRoom.setType(ChatRoomType.DEPARTMENT);
            newRoom.setDepartment(department);
            return roomRepo.save(newRoom);
        });

        Employee manager = department.getManager();

        // Reset ADMIN cũ về MEMBER trước khi gán manager mới
        memberRepo.findByRoomId(room.getId()).stream()
                .filter(m -> "ADMIN".equals(m.getRole()))
                .forEach(m -> {
                    m.setRole("MEMBER");
                    memberRepo.save(m);
                });

        if (manager != null) {
            userRepo.findByEmployeeId(manager.getId()).ifPresent(managerUser -> {
                if (room.getCreatedBy() == null) {
                    room.setCreatedBy(managerUser);
                    roomRepo.save(room);
                }
                setMemberRole(room.getId(), managerUser.getId(), "ADMIN");
            });
        }

        // Thêm toàn bộ nhân viên ACTIVE vào phòng (bỏ qua manager và admin hệ thống)
        List<Employee> employees = employeeRepo.findByDepartmentIdAndStatus(
                department.getId(), EmployeeStatus.ACTIVE);
        for (Employee emp : employees) {
            if (manager != null && emp.getId().equals(manager.getId()))
                continue;
            userRepo.findByEmployeeId(emp.getId())
                    .filter(user -> user.getRole() != Role.ADMIN)
                    .ifPresent(user -> addMemberWithRole(room.getId(), user.getId(), "MEMBER"));
        }

        // Gửi tin hệ thống lần đầu khi phòng mới được tạo
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

        addMemberWithRole(saved.getId(), creatorUserId, "ADMIN");

        if (memberUserIds != null) {
            for (Long uid : memberUserIds) {
                if (!uid.equals(creatorUserId)) {
                    addMemberWithRole(saved.getId(), uid, "MEMBER");
                }
            }
        }

        String sysMsg = getDisplayName(creator) + " đã tạo nhóm \"" + name + "\"";
        sendSystemMessage(saved.getId(), sysMsg);
        return saved;
    }

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

        String sysMsg = getDisplayName(updater) + " đã đổi tên nhóm từ \"" + oldName + "\" thành \"" + newName + "\"";
        sendSystemMessage(roomId, sysMsg);
        return saved;
    }

    // Thêm nhiều thành viên vào nhóm, không cho phép thêm admin hệ thống
    @Transactional
    public void addMembers(Long roomId, List<Long> userIds, Long addedByUserId) {
        User addedBy = userRepo.findById(addedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        if (!memberRepo.existsByRoomIdAndUserId(roomId, addedByUserId)) {
            throw new ForbiddenException("Bạn không trong phòng chat này");
        }

        for (Long uid : userIds) {
            User newUser = userRepo.findById(uid).orElse(null);
            if (newUser == null)
                continue;
            if (newUser.getRole() == Role.ADMIN) {
                throw new ForbiddenException("Không thể thêm tài khoản admin vào phòng chat");
            }
            if (!memberRepo.existsByRoomIdAndUserId(roomId, uid)) {
                addMemberWithRole(roomId, uid, "MEMBER");
                String sysMsg = getDisplayName(addedBy) + " đã thêm " + getDisplayName(newUser) + " vào nhóm";
                sendSystemMessage(roomId, sysMsg);
            }
        }
    }

    @Transactional
    public void removeMemberFromRoom(Long roomId, Long userId, Long removedByUserId) {
        User removedBy = userRepo.findById(removedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        User target = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        String sysMsg;
        if (userId.equals(removedByUserId)) {
            sysMsg = getDisplayName(target) + " đã rời khỏi nhóm";
        } else {
            sysMsg = getDisplayName(removedBy) + " đã xóa " + getDisplayName(target) + " khỏi nhóm";
        }

        memberRepo.deleteByRoomIdAndUserId(roomId, userId);
        sendSystemMessage(roomId, sysMsg);
    }

    // Dùng nội bộ service, đã lọc admin hệ thống
    public List<ChatRoomMember> getRoomMembers(Long roomId) {
        return memberRepo.findByRoomId(roomId).stream()
                .filter(m -> m.getUser().getRole() != Role.ADMIN)
                .collect(Collectors.toList());
    }

    // Trả về DTO thành viên kèm họ tên, chức vụ, phòng ban (dùng cho REST response)
    public List<RoomMemberDTO> getRoomMembersDTO(Long roomId) {
        return memberRepo.findByRoomId(roomId).stream()
                .filter(m -> m.getUser().getRole() != Role.ADMIN)
                .map(m -> {
                    RoomMemberDTO dto = RoomMemberDTO.from(m);
                    dto.setFullName(getDisplayName(m.getUser()));
                    Long empId = m.getUser().getEmployeeId();
                    if (empId != null) {
                        employeeRepo.findById(empId).ifPresent(emp -> {
                            dto.setPosition(emp.getPosition());
                            if (emp.getDepartment() != null) {
                                dto.setDepartmentName(emp.getDepartment().getName());
                            }
                        });
                    }
                    return dto;
                }).collect(Collectors.toList());
    }

    public List<Message> searchMessages(Long roomId, String keyword) {
        return messageRepo.searchMessages(roomId, keyword);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────

    // Thêm thành viên vào phòng với role chỉ định, bỏ qua nếu đã có
    private void addMemberWithRole(Long roomId, Long userId, String role) {
        if (!memberRepo.existsByRoomIdAndUserId(roomId, userId)) {
            ChatRoomMember member = new ChatRoomMember();
            roomRepo.findById(roomId).ifPresent(member::setRoom);
            userRepo.findById(userId).ifPresent(member::setUser);
            member.setRole(role);
            memberRepo.save(member);
        }
    }

    // Cập nhật role nếu đã trong phòng, thêm mới nếu chưa có
    private void setMemberRole(Long roomId, Long userId, String role) {
        memberRepo.findByRoomIdAndUserId(roomId, userId).ifPresentOrElse(
                member -> {
                    member.setRole(role);
                    memberRepo.save(member);
                },
                () -> addMemberWithRole(roomId, userId, role));
    }

    // Lấy họ tên thật từ Employee, fallback về username nếu không có
    private String getDisplayName(User user) {
        Long empId = user.getEmployeeId();
        if (empId != null) {
            return employeeRepo.findById(empId)
                    .map(emp -> emp.getFullName())
                    .orElse(user.getUsername());
        }
        return user.getUsername();
    }

    // Dùng cho Controller khi cần resolve tên theo userId
    public String resolveDisplayName(Long userId) {
        return userRepo.findById(userId).map(this::getDisplayName).orElse("Unknown");
    }

    private MessageDTO enrichMessageDTO(MessageDTO dto, Message m) {
        if (m.getSender() != null) {
            dto.setSenderName(getDisplayName(m.getSender()));
        }
        return dto;
    }

    // Gửi tin nhắn hệ thống vào phòng (dùng creator của room làm sender)
    private void sendSystemMessage(Long roomId, String content) {
        ChatRoom room = roomRepo.findById(roomId).orElse(null);
        if (room == null)
            return;

        User systemUser = room.getCreatedBy();
        if (systemUser == null)
            return;

        Message sys = new Message();
        sys.setRoom(room);
        sys.setSender(systemUser);
        sys.setMessage(content);
        sys.setMessageType(MessageType.SYSTEM);
        Message saved = messageRepo.save(sys);

        broadcastEvent(roomId, ChatEventType.NEW_MESSAGE, enrichMessageDTO(MessageDTO.from(saved), saved), null,
                systemUser.getId());
    }

    public void addMember(Long roomId, Long userId) {
        addMemberWithRole(roomId, userId, "MEMBER");
    }

    // ══════════════════════════════════════════════════════════════
    // TIN NHẮN
    // ══════════════════════════════════════════════════════════════

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

        // Cập nhật lastReadMessageId của người gửi để không tự count tin mình gửi là "chưa đọc"
        memberRepo.findByRoomIdAndUserId(roomId, senderId).ifPresent(member -> {
            member.setLastReadMessageId(saved.getId());
            memberRepo.save(member);
        });

        MessageDTO dto = enrichMessageDTO(MessageDTO.from(saved), saved);
        if (replyToId != null) {
            messageRepo.findById(replyToId)
                    .ifPresent(reply -> dto.setReplyToMessage(enrichMessageDTO(MessageDTO.from(reply), reply)));
        }

        broadcastEvent(roomId, ChatEventType.NEW_MESSAGE, dto, null, senderId);
        return saved;
    }

    public List<Message> getMessages(Long roomId) {
        return messageRepo.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    public Optional<Message> findMessageById(Long messageId) {
        return messageRepo.findById(messageId);
    }

    // ══════════════════════════════════════════════════════════════
    // THU HỒI TIN NHẮN
    // ══════════════════════════════════════════════════════════════

    // Chỉ người gửi mới có quyền thu hồi tin nhắn của mình
    @Transactional
    public Message recallMessage(Long senderId, Long messageId) {
        Message msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin nhắn"));

        if (!msg.getSender().getId().equals(senderId)) {
            throw new ForbiddenException("Chỉ người gửi mới có thể thu hồi tin nhắn");
        }

        msg.setIsRecalled(true);
        msg.setMessage("Tin nhắn đã bị thu hồi");
        Message saved = messageRepo.save(msg);

        broadcastEvent(msg.getRoom().getId(), ChatEventType.RECALL, enrichMessageDTO(MessageDTO.from(saved), saved),
                null, senderId);
        return saved;
    }

    // ══════════════════════════════════════════════════════════════
    // ĐÃ XEM (SEEN)
    // ══════════════════════════════════════════════════════════════

    // Cập nhật lastReadMessageId khi user đọc đến tin cuối cùng trong phòng
    @Transactional
    public void markRoomAsSeen(Long userId, Long roomId, Long lastSeenMessageId) {
        memberRepo.findByRoomIdAndUserId(roomId, userId)
                .ifPresent(member -> {
                    member.setLastReadMessageId(lastSeenMessageId);
                    memberRepo.save(member);
                });
    }

    // ══════════════════════════════════════════════════════════════
    // DANH BẠ
    // ══════════════════════════════════════════════════════════════

    // Tìm kiếm đồng nghiệp theo tên, kỹ năng, chức vụ hoặc trạng thái
    // roomId (optional): nếu truyền vào thì lọc bỏ thành viên đã có trong phòng
    // - Nếu là phòng DEPARTMENT: chỉ trả về nhân viên trong phòng ban đó
    // - Nếu là phòng GROUP/PRIVATE: trả về toàn bộ (trừ người đã trong phòng)
    public List<ContactDTO> searchContacts(Long currentUserId, String keyword, String skill, String position,
            String status, Long roomId) {
        Long currentEmployeeId = userRepo.findById(currentUserId)
                .map(User::getEmployeeId).orElse(null);

        // Lấy danh sách userId đang trong phòng để loại trừ
        Set<Long> existingMemberUserIds = (roomId != null)
                ? memberRepo.findByRoomId(roomId).stream()
                        .map(m -> m.getUser().getId())
                        .collect(Collectors.toSet())
                : Collections.emptySet();

        // Nếu là phòng DEPARTMENT → chỉ lấy nhân viên trong phòng ban đó
        List<Employee> employees;
        if (roomId != null) {
            ChatRoom room = roomRepo.findById(roomId).orElse(null);
            if (room != null && room.getType() == ChatRoomType.DEPARTMENT && room.getDepartment() != null) {
                employees = employeeRepo.findByDepartmentIdAndStatus(
                        room.getDepartment().getId(), EmployeeStatus.ACTIVE);
            } else {
                employees = employeeRepo.findAll();
            }
        } else {
            employees = employeeRepo.findAll();
        }

        return employees.stream()
                .filter(emp -> !emp.getId().equals(currentEmployeeId))
                // Bỏ admin hệ thống khỏi danh bạ chat
                .filter(emp -> userRepo.findByEmployeeId(emp.getId())
                        .map(u -> u.getRole() != Role.ADMIN)
                        .orElse(true))
                // Bỏ những người đang là thành viên hiện tại của phòng
                .filter(emp -> {
                    if (existingMemberUserIds.isEmpty())
                        return true;
                    return userRepo.findByEmployeeId(emp.getId())
                            .map(u -> !existingMemberUserIds.contains(u.getId()))
                            .orElse(true);
                })
                // Lọc theo keyword (tên, email)
                .filter(emp -> keyword == null || keyword.isEmpty()
                        || emp.getFullName().toLowerCase().contains(keyword.toLowerCase())
                        || emp.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .filter(emp -> skill == null || skill.isEmpty()
                        || (emp.getSkills() != null && emp.getSkills().toLowerCase().contains(skill.toLowerCase())))
                .filter(emp -> position == null || position.isEmpty()
                        || emp.getPosition().toLowerCase().contains(position.toLowerCase()))
                .filter(emp -> status == null || status.isEmpty()
                        || emp.getStatus().name().equalsIgnoreCase(status))
                .sorted(Comparator.comparing(
                        emp -> emp.getDepartment() != null ? emp.getDepartment().getName() : "",
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(emp -> {
                    Long userId = userRepo.findByEmployeeId(emp.getId())
                            .map(User::getId).orElse(null);
                    String userStatus = emp.getStatus().name();
                    return ContactDTO.from(emp, userId, userStatus);
                })
                .toList();
    }

    // Broadcast event qua WebSocket đến tất cả thành viên trong phòng
    private void broadcastEvent(Long roomId, ChatEventType eventType,
            MessageDTO message, Object data, Long triggeredBy) {
        ChatEventDTO event = ChatEventDTO.of(eventType.name(), roomId, message, data, triggeredBy);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

}
