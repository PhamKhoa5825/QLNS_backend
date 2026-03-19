package com.example.qlns.Service;

import com.example.qlns.DTO.Request.CreateNotificationRequest;
import com.example.qlns.DTO.Response.NotificationDTO;
import com.example.qlns.Entity.Department;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.Notification;
import com.example.qlns.Entity.User;
import com.example.qlns.Entity.UserNotification;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Enum.NotificationTarget;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notiRepo;
    private final UserNotificationRepository userNotiRepo;
    private final UserRepository userRepo;
    private final EmployeeRepository empRepo;
    private final DepartmentRepository deptRepo;

    NotificationService(NotificationRepository notiRepo, UserNotificationRepository userNotiRepo,
                        UserRepository userRepo, EmployeeRepository empRepo,
                        DepartmentRepository deptRepo) {
        this.notiRepo = notiRepo;
        this.userNotiRepo = userNotiRepo;
        this.userRepo = userRepo;
        this.empRepo = empRepo;
        this.deptRepo = deptRepo;
    }

    /**
     * Tạo thông báo mới với 3 loại target:
     * - COMPANY: gửi cho tất cả users
     * - DEPARTMENT: gửi cho users trong phòng ban
     * - EMPLOYEE: gửi cho 1 nhân viên cụ thể
     */
    @Transactional
    public NotificationDTO createFromRequest(CreateNotificationRequest req) {
        Notification noti = new Notification();
        noti.setTitle(req.getTitle());
        noti.setContent(req.getContent());
        noti.setTargetType(NotificationTarget.valueOf(req.getTargetType()));

        // Set department nếu target = DEPARTMENT
        if (req.getDepartmentId() != null) {
            Department dept = deptRepo.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban"));
            noti.setDepartment(dept);
        }

        // Set targetEmployeeId nếu target = EMPLOYEE
        if (req.getTargetEmployeeId() != null) {
            noti.setTargetEmployeeId(req.getTargetEmployeeId());
        }

        // Set người tạo
        if (req.getCreatedById() != null) {
            Employee creator = empRepo.findById(req.getCreatedById()).orElse(null);
            noti.setCreatedBy(creator);
        }

        Notification saved = notiRepo.save(noti);

        // Tạo UserNotification cho người nhận
        List<User> targets = resolveTargets(noti);
        for (User u : targets) {
            UserNotification un = new UserNotification();
            un.setUser(u);
            un.setNotification(saved);
            userNotiRepo.save(un);
        }

        return NotificationDTO.from(saved, false);
    }

    /** Giải quyết danh sách users nhận thông báo */
    private List<User> resolveTargets(Notification noti) {
        switch (noti.getTargetType()) {
            case COMPANY:
                return userRepo.findAll();

            case DEPARTMENT:
                return empRepo.findByDepartmentIdAndStatus(
                                noti.getDepartment().getId(), EmployeeStatus.ACTIVE)
                        .stream()
                        .map(emp -> userRepo.findByEmail(emp.getEmail()).orElse(null))
                        .filter(u -> u != null)
                        .collect(Collectors.toList());

            case EMPLOYEE:
                if (noti.getTargetEmployeeId() != null) {
                    Employee emp = empRepo.findById(noti.getTargetEmployeeId()).orElse(null);
                    if (emp != null) {
                        User u = userRepo.findByEmail(emp.getEmail()).orElse(null);
                        if (u != null) return List.of(u);
                    }
                }
                return List.of();

            default:
                return List.of();
        }
    }

    /** Cập nhật tiêu đề/nội dung thông báo */
    @Transactional
    public NotificationDTO update(Long id, String title, String content) {
        Notification noti = notiRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
        if (title != null && !title.isBlank()) noti.setTitle(title);
        if (content != null) noti.setContent(content);
        return NotificationDTO.from(notiRepo.save(noti), false);
    }

    /** Xóa thông báo + tất cả UserNotification liên quan */
    @Transactional
    public void delete(Long id) {
        if (!notiRepo.existsById(id))
            throw new ResourceNotFoundException("Không tìm thấy thông báo");
        userNotiRepo.deleteByNotificationId(id);
        notiRepo.deleteById(id);
    }

    /** Lấy thông báo cho nhân viên (cả 3 loại target) */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getForEmployee(Long deptId, Long userId) {
        // Tìm employeeId từ userId
        Long empId = userRepo.findById(userId)
                .map(User::getEmployeeId)
                .orElse(0L);

        List<Notification> notis = notiRepo.findForEmployee(deptId, empId);
        return notis.stream().map(n -> {
            boolean isRead = userNotiRepo.findByUserIdAndNotificationId(userId, n.getId())
                    .map(UserNotification::isRead)
                    .orElse(false);
            return NotificationDTO.from(n, isRead);
        }).collect(Collectors.toList());
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