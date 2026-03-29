package com.example.qlns.Service;

import com.example.qlns.Entity.Notification;
import com.example.qlns.Entity.User;
import com.example.qlns.Entity.UserNotification;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Enum.NotificationTarget;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.NotificationRepository;
import com.example.qlns.Repository.UserNotificationRepository;
import com.example.qlns.Repository.UserRepository;
import com.example.qlns.DTO.Response.NotificationDTO;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notiRepo;
    private final UserNotificationRepository userNotiRepo;
    private final UserRepository userRepo;
    private final EmployeeRepository empRepo;
    private final SimpMessagingTemplate messagingTemplate;

    NotificationService(NotificationRepository notiRepo, UserNotificationRepository userNotiRepo,
                        UserRepository userRepo, EmployeeRepository empRepo, SimpMessagingTemplate messagingTemplate) {
        this.notiRepo = notiRepo;
        this.userNotiRepo = userNotiRepo;
        this.userRepo = userRepo;
        this.empRepo = empRepo;
        this.messagingTemplate = messagingTemplate;
    }

    // ── Admin: Xem tất cả thông báo ────────────────────────────
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAll() {
        return notiRepo.findAll().stream()
                .map(n -> NotificationDTO.from(n, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public Notification create(Notification noti) {
        Notification saved = notiRepo.save(noti);
        // Tạo UserNotification cho từng người nhận
        List<User> targets;
        if (noti.getTargetType() == NotificationTarget.COMPANY) {
            targets = userRepo.findAll();
        } else {
            // Lấy users trong phòng ban
            targets = empRepo.findByDepartmentIdAndStatus(
                            noti.getDepartment().getId(), EmployeeStatus.ACTIVE)
                    .stream()
                    .map(emp -> userRepo.findByEmail(emp.getEmail()).orElse(null))
                    .filter(u -> u != null)
                    .toList();
        }
        for (User u : targets) {
            UserNotification un = new UserNotification();
            un.setUser(u);
            un.setNotification(saved);
            userNotiRepo.save(un);
        }
        return saved;
    }

    @Transactional
    public Notification sendTargetedNotification(User sender, List<User> targets, String title, String content) {
        Notification noti = new Notification();
        noti.setTitle(title);
        noti.setContent(content);
        noti.setTargetType(NotificationTarget.SPECIFIC_USERS);
        
        if (sender != null && sender.getEmployeeId() != null) {
            empRepo.findById(sender.getEmployeeId()).ifPresent(noti::setCreatedBy);
        }

        Notification saved = notiRepo.save(noti);

        for (User u : targets) {
            if (u == null) continue;
            UserNotification un = new UserNotification();
            un.setUser(u);
            un.setNotification(saved);
            userNotiRepo.save(un);
            
            // WebSockets Broadcast
            NotificationDTO dto = NotificationDTO.from(saved, false);
            messagingTemplate.convertAndSend("/topic/notifications/" + u.getId(), dto);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getForEmployee(Long deptId, Long userId) {
        return notiRepo.findForUser(deptId, userId).stream()
                .map(n -> {
                    // Kiểm tra trạng thái đã đọc của user cụ thể
                    boolean isRead = userNotiRepo.findByUserIdAndNotificationId(userId, n.getId())
                            .stream()
                            .anyMatch(un -> un.isRead());
                    return NotificationDTO.from(n, isRead);
                })
                .collect(Collectors.toList());
    }

    public long countUnread(Long userId) {
        return userNotiRepo.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        userNotiRepo.findByUserIdAndNotificationId(userId, notificationId)
                .ifPresent(un -> {
                    un.setRead(true);
                    userNotiRepo.save(un);
                });
    }
}
