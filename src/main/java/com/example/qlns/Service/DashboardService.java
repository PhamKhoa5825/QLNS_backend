package com.example.qlns.Service;

import com.example.qlns.DTO.Response.DepartmentDashboardDTO;
import com.example.qlns.Enum.AttendanceStatus;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Enum.TaskStatus;
import com.example.qlns.Repository.AttendanceRepository;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Service for generating department dashboard statistics.
 */
@Service
public class DashboardService {

    private final EmployeeRepository empRepo;
    private final AttendanceRepository attendanceRepo;
    private final TaskRepository taskRepo;

    public DashboardService(EmployeeRepository empRepo,
                            AttendanceRepository attendanceRepo,
                            TaskRepository taskRepo) {
        this.empRepo = empRepo;
        this.attendanceRepo = attendanceRepo;
        this.taskRepo = taskRepo;
    }

    /**
     * Get dashboard data for a department in a specific month/year.
     */
    @Transactional(readOnly = true)
    public DepartmentDashboardDTO getDashboard(Long deptId, int month, int year) {
        DepartmentDashboardDTO dto = new DepartmentDashboardDTO();

        // ── 1. Tổng số nhân viên ──────────────────────────────
        long totalEmployees = empRepo.countByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE);
        dto.setTotalEmployees(totalEmployees);

        // ── 2. Thống kê chấm công tháng ──────────────────────
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        long onTimeCount = attendanceRepo.countByDepartmentAndStatusBetween(
                deptId, AttendanceStatus.ON_TIME, from, to);
        long lateCount = attendanceRepo.countByDepartmentAndStatusBetween(
                deptId, AttendanceStatus.LATE, from, to);

        // Số ngày làm việc trong tháng (T2-T6) * số NV = tổng lượt cần đi làm
        long workingDaysInMonth = countWorkingDays(from, to);
        long expectedAttendance = workingDaysInMonth * totalEmployees;
        long actualAttendance = onTimeCount + lateCount;
        long absentCount = Math.max(0, expectedAttendance - actualAttendance);

        dto.setOnTimeCount(onTimeCount);
        dto.setLateCount(lateCount);
        dto.setAbsentCount(absentCount);

        if (expectedAttendance > 0) {
            dto.setOnTimeRate(Math.round(onTimeCount * 100.0 / expectedAttendance * 10) / 10.0);
            dto.setLateRate(Math.round(lateCount * 100.0 / expectedAttendance * 10) / 10.0);
            dto.setAbsentRate(Math.round(absentCount * 100.0 / expectedAttendance * 10) / 10.0);
        }

        // ── 3. Thống kê nhiệm vụ ─────────────────────────────
        long taskPending = taskRepo.countByDepartmentAndStatus(deptId, TaskStatus.PENDING)
                         + taskRepo.countByDepartmentAndStatus(deptId, TaskStatus.ACCEPTED);
        long taskCompleted = taskRepo.countByDepartmentAndStatus(deptId, TaskStatus.DONE);
        long taskOverdue = taskRepo.countByDepartmentAndStatus(deptId, TaskStatus.OVERDUE);

        dto.setTaskInProgress(taskPending);
        dto.setTaskPending(taskRepo.countByDepartmentAndStatus(deptId, TaskStatus.PENDING));
        dto.setTaskCompleted(taskCompleted);
        dto.setTaskOverdue(taskOverdue);
        dto.setTaskTotal(taskPending + taskCompleted + taskOverdue);

        return dto;
    }

    /**
     * Count working days (Monday-Friday) between two dates (inclusive).
     */
    private long countWorkingDays(LocalDate from, LocalDate to) {
        long count = 0;
        LocalDate date = from;
        while (!date.isAfter(to)) {
            if (date.getDayOfWeek().getValue() <= 5) { // Mon=1 ... Fri=5
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
