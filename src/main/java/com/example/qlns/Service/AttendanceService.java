package com.example.qlns.Service;

import com.example.qlns.DTO.Response.AttendanceDTO;
import com.example.qlns.Entity.Attendance;
import com.example.qlns.Entity.CompanySettings;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.AttendanceStatus;
import com.example.qlns.Exception.AttendanceException;
import com.example.qlns.Exception.LocationException;
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

    // ── Haversine: tính khoảng cách (mét) giữa 2 toạ độ ─────
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

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

        // Kiểm tra GPS
        CompanySettings settings = getSettings();
        double distance = haversine(lat, lng, settings.getBaseLat(), settings.getBaseLng());
        if (distance > settings.getAllowedRadius())
            throw new LocationException(String.format(
                    "Ngoài phạm vi %.0fm (bạn cách văn phòng %.0fm)",
                    settings.getAllowedRadius(), distance));

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

    public List<Attendance> getByEmployeeAndMonth(Long empId, int month, int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        return attendanceRepo.findByEmployeeIdAndDateBetween(empId, from, to);
    }

    public long countWorkingDays(Long empId, int month, int year) {
        return attendanceRepo.countWorkingDays(empId, month, year);
    }
}