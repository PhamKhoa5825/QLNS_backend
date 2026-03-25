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
 * AdminDashboardController — API gộp thống kê toàn hệ thống
 *
 * Thay vì FE gọi 5-6 API riêng rồi count, giờ chỉ cần 1 call:
 * GET /api/admin/dashboard/stats
 *
 * Trả về JSON gộp tất cả: NV, PB, task, đơn, chấm công today
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

    /**
     * GET /api/admin/dashboard/stats
     *
     * Response:
     * {
     *   "employees": { "total": 10, "active": 8, "resigned": 2 },
     *   "departments": { "total": 5 },
     *   "tasks": { "pending": 3, "accepted": 2, "done": 10, "overdue": 1 },
     *   "requests": { "pending": 2, "approved": 5, "rejected": 1 },
     *   "attendance": { "today": { "onTime": 6, "late": 2, "absent": 0, "total": 8 } }
     * }
     */
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
        Map<String, Long> deptStats = new HashMap<>();
        deptStats.put("total", deptRepo.count());
        stats.put("departments", deptStats);

        // ── Nhiệm vụ (task) ──
        Map<String, Long> taskStats = new HashMap<>();
        taskStats.put("pending", taskRepo.countByStatus(TaskStatus.PENDING));
        taskStats.put("accepted", taskRepo.countByStatus(TaskStatus.ACCEPTED));
        taskStats.put("done", taskRepo.countByStatus(TaskStatus.DONE));
        taskStats.put("overdue", taskRepo.countByStatus(TaskStatus.OVERDUE));
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
        long totalActiveForAttendance = activeEmp; // Tổng NV active
        long checkedIn = todayRecords.size();
        long notCheckedIn = Math.max(0, totalActiveForAttendance - checkedIn);

        attendanceStats.put("onTime", onTime);
        attendanceStats.put("late", late);
        attendanceStats.put("notCheckedIn", notCheckedIn);
        attendanceStats.put("totalCheckedIn", checkedIn);
        stats.put("attendanceToday", attendanceStats);

        return ResponseEntity.ok(stats);
    }
}
