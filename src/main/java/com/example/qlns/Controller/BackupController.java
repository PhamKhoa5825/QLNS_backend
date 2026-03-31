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
 * BackupController — Quản lý sao lưu / khôi phục Database.
 */
@RestController
@RequestMapping("/api/admin/backup")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBackup() {
        try {
            return ResponseEntity.ok(backupService.createBackup());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listBackups() {
        try {
            return ResponseEntity.ok(backupService.listBackups());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/{filename}/download")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String filename) {
        try {
            Path filePath = backupService.getBackupFile(filename);
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{filename}/restore")
    public ResponseEntity<Map<String, Object>> restoreBackup(@PathVariable String filename) {
        try {
            return ResponseEntity.ok(backupService.restoreBackup(filename));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> deleteBackup(@PathVariable String filename) {
        try {
            backupService.deleteBackup(filename);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBackupStatus() {
        return ResponseEntity.ok(backupService.getStatus());
    }
}
