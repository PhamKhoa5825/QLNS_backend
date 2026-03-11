package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CheckInRequest;
import com.example.qlns.DTO.Request.CheckOutRequest;
import com.example.qlns.DTO.Response.AttendanceDTO;
import com.example.qlns.Entity.Attendance;
import com.example.qlns.Service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV3 - AttendanceController
// =============================================
@RestController
@RequestMapping("/api/attendance")
class AttendanceController {
    private final AttendanceService attendanceService;

    AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/checkin")
    public ResponseEntity<AttendanceDTO> checkIn(@RequestBody CheckInRequest req) {
        Attendance att = attendanceService.checkIn(req.getEmployeeId(), req.getLatitude(), req.getLongitude());
        return ResponseEntity.ok(AttendanceDTO.from(att));
    }

    @PutMapping("/checkout")
    public ResponseEntity<AttendanceDTO> checkOut(@RequestBody CheckOutRequest req) {
        Attendance att = attendanceService.checkOut(req.getEmployeeId());
        return ResponseEntity.ok(AttendanceDTO.from(att));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<AttendanceDTO>> getByEmployee(@PathVariable Long empId) {
        return ResponseEntity.ok(attendanceService.getByEmployee(empId).stream()
                .map(AttendanceDTO::from).collect(Collectors.toList()));
    }

    @GetMapping("/employee/{empId}/month")
    public ResponseEntity<List<AttendanceDTO>> getByMonth(@PathVariable Long empId,
                                                          @RequestParam int month,
                                                          @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.getByEmployeeAndMonth(empId, month, year).stream()
                .map(AttendanceDTO::from).collect(Collectors.toList()));
    }

    @GetMapping("/employee/{empId}/working-days")
    public ResponseEntity<Long> countWorkingDays(@PathVariable Long empId,
                                                 @RequestParam int month,
                                                 @RequestParam int year) {
        return ResponseEntity.ok(attendanceService.countWorkingDays(empId, month, year));
    }

    @GetMapping("/today")
    public ResponseEntity<List<AttendanceDTO>> getToday() {
        return ResponseEntity.ok(attendanceService.getByDate(LocalDate.now()).stream()
                .map(AttendanceDTO::from).collect(Collectors.toList()));
    }
}
