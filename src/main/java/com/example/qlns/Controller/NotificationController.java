package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateNotificationRequest;
import com.example.qlns.DTO.Response.NotificationDTO;
import com.example.qlns.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
class NotificationController {
    private final NotificationService notiService;

    NotificationController(NotificationService notiService) {
        this.notiService = notiService;
    }

    /** GET /api/notifications/department/{deptId}?userId=X */
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<NotificationDTO>> getForEmployee(
            @PathVariable Long deptId, @RequestParam Long userId) {
        return ResponseEntity.ok(notiService.getForEmployee(deptId, userId));
    }

    /** POST /api/notifications — tạo thông báo mới (COMPANY/DEPARTMENT/EMPLOYEE) */
    @PostMapping
    public ResponseEntity<NotificationDTO> create(@RequestBody CreateNotificationRequest req) {
        return ResponseEntity.ok(notiService.createFromRequest(req));
    }

    /** PUT /api/notifications/{id} — sửa tiêu đề/nội dung */
    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTO> update(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(notiService.update(id,
                body.get("title"), body.get("content")));
    }

    /** DELETE /api/notifications/{id} — xóa thông báo */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notiService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** PUT /api/notifications/{id}/read?userId=X — đánh dấu đã đọc */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        notiService.markAsRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/notifications/unread-count?userId=X */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(notiService.countUnread(userId));
    }
}