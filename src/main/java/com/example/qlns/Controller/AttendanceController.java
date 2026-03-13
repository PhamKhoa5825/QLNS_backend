package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CheckInRequest;
import com.example.qlns.DTO.Request.CheckOutRequest;
import com.example.qlns.DTO.Response.AttendanceDTO;
import com.example.qlns.DTO.Response.AttendanceStatsDTO;
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

    // POST /api/attendance/checkin
    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDTO> checkIn(@RequestBody CheckInRequest req) {
        // Service đã trả về AttendanceDTO → không cần gọi AttendanceDTO.from() lại
        return ResponseEntity.ok(attendanceService.checkIn(
                req.getEmployeeId(), req.getLatitude(), req.getLongitude()));
    }

    // PUT /api/attendance/checkout
    @PutMapping("/checkout")
    public ResponseEntity<AttendanceDTO> checkOut(@RequestBody CheckOutRequest req) {
        // Service đã trả về AttendanceDTO → không cần gọi AttendanceDTO.from() lại
        return ResponseEntity.ok(attendanceService.checkOut(req.getEmployeeId()));
    }

    // GET /api/attendance/employee/{empId}
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<AttendanceDTO>> getByEmployee(@PathVariable Long empId) {
        // Service trả về List<Attendance> → convert sang DTO tại đây
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

    // GET /api/attendance/today
    @GetMapping("/today")
    public ResponseEntity<List<AttendanceDTO>> getToday() {
        return ResponseEntity.ok(
                attendanceService.getByDate(LocalDate.now())
                        .stream()
                        .map(AttendanceDTO::from)
                        .collect(Collectors.toList()));
    }

    // GET /api/attendance/employee/{empId}/stats?month=3&year=2026
    @GetMapping("/employee/{empId}/stats")
    public ResponseEntity<AttendanceStatsDTO> getStats(
            @PathVariable Long empId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getMonthlyStats(empId, month, year));
    }
}