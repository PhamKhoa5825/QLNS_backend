package com.example.qlns.Controller;

import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.SystemLog;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.SystemLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/logs")
class SystemLogController {
    private final SystemLogRepository logRepo;
    private final EmployeeRepository employeeRepo;

    SystemLogController(SystemLogRepository logRepo, EmployeeRepository employeeRepo) {
        this.logRepo = logRepo;
        this.employeeRepo = employeeRepo;
    }

    /** Lấy 50 log gần nhất */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getRecentLogs() {
        List<Map<String, Object>> result = logRepo.findTop50ByOrderByCreatedAtDesc()
                .stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** Lọc theo action */
    @GetMapping("/filter")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> filterByAction(@RequestParam("action") String action) {
        List<Map<String, Object>> result = logRepo.findByAction(action)
                .stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Chuyển SystemLog → Map.
     * @Transactional giữ session mở → log.getUser() không bị LazyInitializationException.
     */
    private Map<String, Object> toMap(SystemLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("action", log.getAction());
        map.put("description", log.getDescription());
        map.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
        
        String username = "System";
        String department = null;
        String position = null;

        if (log.getUser() != null) {
            username = log.getUser().getUsername();
            Long empId = log.getUser().getEmployeeId();
            if (empId != null) {
                Employee emp = employeeRepo.findById(empId).orElse(null);
                if (emp != null) {
                    position = emp.getPosition();
                    if (emp.getDepartment() != null) {
                        department = emp.getDepartment().getName();
                    }
                }
            }
        }

        map.put("username", username);
        map.put("department", department);
        map.put("position", position);
        
        return map;
    }
}
