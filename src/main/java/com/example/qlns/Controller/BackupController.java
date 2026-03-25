package com.example.qlns.Controller;

import com.example.qlns.Service.BackupService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * BackupController — Quản lý sao lưu / khôi phục database
 *
 * Endpoints:
 * - POST   /api/admin/backup              → Tạo backup thủ công
 * - GET    /api/admin/backup/list          → Danh sách backup
 * - GET    /api/admin/backup/{name}/download → Download file .sql
 * - POST   /api/admin/backup/{name}/restore → Restore (GHI ĐÈ!)
 * - DELETE /api/admin/backup/{name}        → Xóa backup
 *
 * Tất cả đều yêu cầu ROLE_ADMIN (cấu hình trong SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/backup")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    /**
     * POST /api/admin/backup
     * Tạo bản sao lưu database ngay lập tức.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBackup() {
        try {
            Map<String, Object> result = backupService.createBackup();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Tạo backup thất bại: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/backup/list
     * Liệt kê tất cả bản sao lưu (mới nhất trước).
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listBackups() {
        try {
            return ResponseEntity.ok(backupService.listBackups());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    /**
     * GET /api/admin/backup/{filename}/download
     * Download file .sql backup.
     */
    @GetMapping("/{filename}/download")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String filename) {
        try {
            Path filePath = backupService.getBackupFile(filename);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (SecurityException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/admin/backup/{filename}/restore
     * Khôi phục database từ bản sao lưu.
     * CẢNH BÁO: Ghi đè toàn bộ dữ liệu hiện tại!
     */
    @PostMapping("/{filename}/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(@PathVariable String filename) {
        try {
            Map<String, Object> result = backupService.restoreBackup(filename);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Khôi phục thất bại: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/backup/{filename}
     * Xóa bản sao lưu.
     */
    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> deleteBackup(@PathVariable String filename) {
        try {
            backupService.deleteBackup(filename);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
