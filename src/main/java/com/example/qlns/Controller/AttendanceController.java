package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CheckInRequest;
import com.example.qlns.DTO.Request.CheckOutRequest;
import com.example.qlns.DTO.Response.AttendanceDTO;
import com.example.qlns.Security.SecurityService;
import com.example.qlns.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private SecurityService securityService;

    // POST /api/attendance/checkin
    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDTO> checkIn(@RequestBody CheckInRequest req) {
        return ResponseEntity.ok(attendanceService.checkIn(
                req.getEmployeeId(), req.getLatitude(), req.getLongitude()));
    }

    // PUT /api/attendance/checkout
    @PutMapping("/checkout")
    public ResponseEntity<AttendanceDTO> checkOut(@RequestBody CheckOutRequest req) {
        return ResponseEntity.ok(attendanceService.checkOut(req.getEmployeeId()));
    }

    // GET /api/attendance/employee/{empId}
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<AttendanceDTO>> getByEmployee(@PathVariable Long empId) {
        return ResponseEntity.ok(
                attendanceService.getByEmployee(empId)
                        .stream()
                        .map(AttendanceDTO::from)
                        .collect(Collectors.toList()));
    }

    // GET /api/attendance/employee/{empId}/month?month=3&year=2026
    @GetMapping("/employee/{empId}/month")
    public ResponseEntity<List<AttendanceDTO>> getByMonth(
            @PathVariable Long empId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(
                attendanceService.getByEmployeeAndMonth(empId, month, year)
                        .stream()
                        .map(AttendanceDTO::from)
                        .collect(Collectors.toList()));
    }

    // GET /api/attendance/employee/{empId}/working-days?month=3&year=2026
    @GetMapping("/employee/{empId}/working-days")
    public ResponseEntity<Long> countWorkingDays(
            @PathVariable Long empId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.countWorkingDays(empId, month, year));
    }

    // GET /api/attendance/today — toàn bộ (Admin only trong SecurityConfig)
    @GetMapping("/today")
    public ResponseEntity<List<AttendanceDTO>> getToday() {
        return ResponseEntity.ok(
                attendanceService.getByDate(LocalDate.now())
                        .stream()
                        .map(AttendanceDTO::from)
                        .collect(Collectors.toList()));
    }

    // GET /api/attendance/today/department/{deptId} — theo phòng ban (Manager)
    @GetMapping("/today/department/{deptId}")
    public ResponseEntity<List<AttendanceDTO>> getTodayByDepartment(@PathVariable Long deptId) {
        // Manager chỉ xem được chấm công phòng ban mình
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(
                attendanceService.getByDateAndDepartment(LocalDate.now(), deptId)
                        .stream()
                        .map(AttendanceDTO::from)
                        .collect(Collectors.toList()));
    }

    // GET /api/attendance/department/{deptId}/date?date=2026-03-10 — theo phòng ban và ngày cụ thể
    @GetMapping("/department/{deptId}/date")
    public ResponseEntity<List<AttendanceDTO>> getByDepartmentAndDate(
            @PathVariable Long deptId,
            @RequestParam String date) {
        securityService.validateManagerDepartment(deptId);
        LocalDate targetDate = LocalDate.parse(date);
        return ResponseEntity.ok(
                attendanceService.getByDateAndDepartment(targetDate, deptId)
                        .stream()
                        .map(AttendanceDTO::from)
                        .collect(Collectors.toList()));
    }

    // GET /api/attendance/employee/{empId}/statistics
    @GetMapping("/employee/{empId}/statistics")
    public ResponseEntity<com.example.qlns.DTO.Response.AttendanceStatsDTO> getEmployeeStats(
            @PathVariable Long empId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getEmployeeStats(empId, month, year));
    }

    // GET /api/attendance/department/{deptId}/statistics
    @GetMapping("/department/{deptId}/statistics")
    public ResponseEntity<List<com.example.qlns.DTO.Response.EmployeeAttendanceStatsDTO>> getDepartmentStats(
            @PathVariable Long deptId,
            @RequestParam int month,
            @RequestParam int year) {
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(attendanceService.getDepartmentStats(deptId, month, year));
    }
}