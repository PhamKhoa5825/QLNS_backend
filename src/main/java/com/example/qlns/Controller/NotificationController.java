package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateNotificationRequest;
import com.example.qlns.DTO.Response.NotificationDTO;
import com.example.qlns.Entity.Notification;
import com.example.qlns.Enum.NotificationTarget;
import com.example.qlns.Service.EmployeeService;
import com.example.qlns.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV4 - NotificationController
// =============================================
@RestController
@RequestMapping("/api/notifications")
class NotificationController {
    private final NotificationService notiService;

    NotificationController(NotificationService notiService) {
        this.notiService = notiService;
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<NotificationDTO>> getForEmployee(@PathVariable Long deptId,
                                                                @RequestParam Long userId) {
        List<Notification> notis = notiService.getForEmployee(deptId);
        return ResponseEntity.ok(notis.stream()
                .map(n -> {
                    // Lấy trạng thái đọc của user này
                    boolean isRead = false; // TV4 tự implement lookup từ userNotiRepo
                    return NotificationDTO.from(n, isRead);
                })
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> create(@RequestBody CreateNotificationRequest req,
                                                  @Autowired EmployeeService empService) {
        Notification noti = new Notification();
        noti.setTitle(req.getTitle());
        noti.setContent(req.getContent());
        noti.setTargetType(NotificationTarget.valueOf(req.getTargetType()));
        if (req.getCreatedById() != null) noti.setCreatedBy(empService.getById(req.getCreatedById()));
        return ResponseEntity.ok(NotificationDTO.from(notiService.create(noti), false));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        notiService.markAsRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(notiService.countUnread(userId));
    }
}
