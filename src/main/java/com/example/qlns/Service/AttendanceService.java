package com.example.qlns.Service;

import com.example.qlns.DTO.Response.AttendanceDTO;
import com.example.qlns.Entity.Attendance;
import com.example.qlns.Entity.CompanySettings;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.AttendanceStatus;
import com.example.qlns.Exception.AttendanceException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.AttendanceRepository;
import com.example.qlns.Repository.CompanySettingsRepository;
import com.example.qlns.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private EmployeeRepository empRepo;
    @Autowired private CompanySettingsRepository settingsRepo;

    // ── Lấy settings, ném lỗi nếu chưa cấu hình ─────────────
    private CompanySettings getSettings() {
        return settingsRepo.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa cấu hình công ty"));
    }

    // ── CHECK IN ─────────────────────────────────────────────
    @Transactional
    public AttendanceDTO checkIn(Long employeeId, Double lat, Double lng) {
        Employee emp = empRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        LocalDate today = LocalDate.now();
        if (attendanceRepo.existsByEmployeeIdAndDate(employeeId, today))
            throw new AttendanceException("Đã chấm công hôm nay rồi");

        CompanySettings settings = getSettings();

        // Tính trạng thái và số phút trễ
        LocalTime startTime    = LocalTime.parse(settings.getWorkStartTime()); // "08:00"
        LocalTime nowTime      = LocalTime.now();
        int lateMinutes        = 0;
        AttendanceStatus status = AttendanceStatus.ON_TIME;

        if (nowTime.isAfter(startTime)) {
            lateMinutes = (int) Duration.between(startTime, nowTime).toMinutes();
            status      = AttendanceStatus.LATE;
        }

        Attendance att = new Attendance();
        att.setEmployee(emp);
        att.setDate(today);
        att.setCheckIn(LocalDateTime.now());
        att.setLocationLat(lat);
        att.setLocationLng(lng);
        att.setStatus(status);
        att.setLateMinutes(lateMinutes);

        return AttendanceDTO.from(attendanceRepo.save(att));
    }

    // ── CHECK OUT ────────────────────────────────────────────
    @Transactional
    public AttendanceDTO checkOut(Long employeeId) {
        Attendance att = attendanceRepo
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .orElseThrow(() -> new AttendanceException("Bạn chưa check-in hôm nay"));

        if (att.getCheckOut() != null)
            throw new AttendanceException("Bạn đã check-out rồi");

        // Chỉ cho check-out sau giờ tan ca
        CompanySettings settings = getSettings();
        LocalTime endTime = LocalTime.parse(settings.getWorkEndTime()); // "17:30"
        if (LocalTime.now().isBefore(endTime))
            throw new AttendanceException(
                    "Chưa đến giờ check-out. Giờ tan ca: " + settings.getWorkEndTime());

        LocalDateTime checkOut = LocalDateTime.now();
        double workHours = Duration.between(att.getCheckIn(), checkOut).toMinutes() / 60.0;

        att.setCheckOut(checkOut);
        att.setWorkHours((float) workHours);

        return AttendanceDTO.from(attendanceRepo.save(att));
    }

    // ── QUERY ────────────────────────────────────────────────
    public List<Attendance> getByEmployee(Long empId) {
        return attendanceRepo.findByEmployeeId(empId);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepo.findByDate(date);
    }

    public List<Attendance> getByDateAndDepartment(LocalDate date, Long deptId) {
        return attendanceRepo.findByDateAndEmployeeDepartmentId(date, deptId);
    }

    public List<Attendance> getByEmployeeAndMonth(Long empId, int month, int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        return attendanceRepo.findByEmployeeIdAndDateBetween(empId, from, to);
    }

    public long countWorkingDays(Long empId, int month, int year) {
        return attendanceRepo.countWorkingDays(empId, month, year);
    }

    public com.example.qlns.DTO.Response.AttendanceStatsDTO getEmployeeStats(Long empId, int month, int year) {
        long onTime = attendanceRepo.countByEmployeeAndStatus(empId, month, year, AttendanceStatus.ON_TIME);
        long late = attendanceRepo.countByEmployeeAndStatus(empId, month, year, AttendanceStatus.LATE);
        // Simplification for absent: assuming 22 working days per month minus (onTime + late), minimum 0
        long absent = Math.max(0, 22 - (onTime + late)); 
        return new com.example.qlns.DTO.Response.AttendanceStatsDTO(onTime, late, absent);
    }

    public List<com.example.qlns.DTO.Response.EmployeeAttendanceStatsDTO> getDepartmentStats(Long deptId, int month, int year) {
        List<Employee> employees = empRepo.findByDepartmentIdAndStatus(deptId, com.example.qlns.Enum.EmployeeStatus.ACTIVE);
        return employees.stream().map(emp -> {
            long onTime = attendanceRepo.countByEmployeeAndStatus(emp.getId(), month, year, AttendanceStatus.ON_TIME);
            long late = attendanceRepo.countByEmployeeAndStatus(emp.getId(), month, year, AttendanceStatus.LATE);
            long absent = Math.max(0, 22 - (onTime + late));
            return new com.example.qlns.DTO.Response.EmployeeAttendanceStatsDTO(emp.getId(), emp.getFullName(), onTime, late, absent);
        }).collect(java.util.stream.Collectors.toList());
    }
}