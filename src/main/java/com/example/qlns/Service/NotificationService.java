package com.example.qlns.Service;

import com.example.qlns.Entity.*;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// =============================================
// 6. NOTIFICATION SERVICE (TV4 viết)
// =============================================
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    public Notification send(Long employeeId, String title, String content,
                             Notification.NotificationType type, Long referenceId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", employeeId));
        return notificationRepository.save(
                new Notification(title, content, type, employee, referenceId));
    }

    public void sendToList(List<Long> employeeIds, String title, String content,
                           Notification.NotificationType type) {
        employeeIds.forEach(id -> {
            try { send(id, title, content, type, null); }
            catch (ResourceNotFoundException ignored) {}
        });
    }

    public List<Notification> getMyNotifications(Long employeeId) {
        return notificationRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    public long getUnreadCount(Long employeeId) {
        return notificationRepository.countByEmployeeIdAndIsReadFalse(employeeId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo", notificationId));
        n.setRead(true);
        notificationRepository.save(n);
    }
}
