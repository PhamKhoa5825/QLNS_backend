package com.example.qlns.Controller;

import com.example.qlns.Enum.*;
import com.example.qlns.Repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * AdminDashboardController — API gộp thống kê toàn hệ thống cho Admin
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final EmployeeRepository empRepo;
    private final DepartmentRepository deptRepo;
    private final TaskRepository taskRepo;
    private final RequestRepository requestRepo;
    private final AttendanceRepository attendanceRepo;

    public AdminDashboardController(EmployeeRepository empRepo,
                                     DepartmentRepository deptRepo,
                                     TaskRepository taskRepo,
                                     RequestRepository requestRepo,
                                     AttendanceRepository attendanceRepo) {
        this.empRepo = empRepo;
        this.deptRepo = deptRepo;
        this.taskRepo = taskRepo;
        this.requestRepo = requestRepo;
        this.attendanceRepo = attendanceRepo;
    }

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // ── Nhân viên ──
        Map<String, Long> empStats = new HashMap<>();
        long totalEmp = empRepo.count();
        long activeEmp = empRepo.countByStatus(EmployeeStatus.ACTIVE);
        empStats.put("total", totalEmp);
        empStats.put("active", activeEmp);
        empStats.put("resigned", totalEmp - activeEmp);
        stats.put("employees", empStats);

        // ── Phòng ban ──
        stats.put("departments", Map.of("total", deptRepo.count()));

        // ── Nhiệm vụ (task) ──
        Map<String, Long> taskStats = new HashMap<>();
        taskStats.put("pending", taskRepo.countByStatus(TaskStatus.PENDING));
        taskStats.put("accepted", taskRepo.countByStatus(TaskStatus.ACCEPTED));
        taskStats.put("done", taskRepo.countByStatus(TaskStatus.DONE));
        stats.put("tasks", taskStats);

        // ── Đơn từ ──
        Map<String, Long> requestStats = new HashMap<>();
        requestStats.put("pending", requestRepo.countByStatus(RequestStatus.PENDING));
        requestStats.put("approved", requestRepo.countByStatus(RequestStatus.APPROVED));
        requestStats.put("rejected", requestRepo.countByStatus(RequestStatus.REJECTED));
        stats.put("requests", requestStats);

        // ── Chấm công hôm nay ──
        Map<String, Object> attendanceStats = new HashMap<>();
        LocalDate today = LocalDate.now();
        var todayRecords = attendanceRepo.findByDate(today);
        long onTime = todayRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_TIME).count();
        long late = todayRecords.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        
        attendanceStats.put("onTime", onTime);
        attendanceStats.put("late", late);
        attendanceStats.put("totalCheckedIn", (long) todayRecords.size());
        stats.put("attendanceToday", attendanceStats);

        return ResponseEntity.ok(stats);
    }
}
