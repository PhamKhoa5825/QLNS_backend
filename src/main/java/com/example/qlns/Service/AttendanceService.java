package com.example.qlns.Service;

import com.example.qlns.Entity.Attendance;
import com.example.qlns.Entity.CompanySettings;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.AttendanceStatus;
import com.example.qlns.Exception.AttendanceException;
import com.example.qlns.Exception.LocationException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.AttendanceRepository;
import com.example.qlns.Repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// =============================================
// TV3 - AttendanceService
// =============================================
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository empRepo;
    private final CompanySettingsService settingsService;

    AttendanceService(AttendanceRepository attendanceRepo, EmployeeRepository empRepo,
                      CompanySettingsService settingsService) {
        this.attendanceRepo = attendanceRepo;
        this.empRepo = empRepo;
        this.settingsService = settingsService;
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Transactional
    public Attendance checkIn(Long employeeId, Double lat, Double lng) {
        Employee emp = empRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        LocalDate today = LocalDate.now();
        if (attendanceRepo.existsByEmployeeIdAndDate(employeeId, today))
            throw new AttendanceException("Đã chấm công hôm nay rồi");

        CompanySettings settings = settingsService.get();
        double distance = haversine(lat, lng, settings.getBaseLat(), settings.getBaseLng());
        if (distance > settings.getAllowedRadius())
            throw new LocationException(String.format(
                    "Ngoài phạm vi %.0fm (bạn cách văn phòng %.0fm)", settings.getAllowedRadius(), distance));

        LocalDateTime now = LocalDateTime.now();
        String[] parts = settings.getWorkStartTime().split(":");
        LocalDateTime startTime = today.atTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        AttendanceStatus status = now.isAfter(startTime) ? AttendanceStatus.LATE : AttendanceStatus.ON_TIME;

        Attendance att = new Attendance();
        att.setEmployee(emp);
        att.setDate(today);
        att.setCheckIn(now);
        att.setLocationLat(lat);
        att.setLocationLng(lng);
        att.setStatus(status);
        return attendanceRepo.save(att);
    }

    @Transactional
    public Attendance checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance att = attendanceRepo.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new AttendanceException("Chưa check-in hôm nay"));
        if (att.getCheckOut() != null)
            throw new AttendanceException("Đã check-out rồi");

        LocalDateTime now = LocalDateTime.now();
        att.setCheckOut(now);

        // Tính giờ làm việc
        float hours = (float) Duration.between(att.getCheckIn(), now).toMinutes() / 60;
        att.setWorkHours(Math.round(hours * 10) / 10f);
        return attendanceRepo.save(att);
    }

    public List<Attendance> getByEmployee(Long empId) {
        return attendanceRepo.findByEmployeeId(empId);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepo.findByDate(date);
    }

    public List<Attendance> getByEmployeeAndMonth(Long empId, int month, int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return attendanceRepo.findByEmployeeIdAndDateBetween(empId, from, to);
    }

    public long countWorkingDays(Long empId, int month, int year) {
        return attendanceRepo.countWorkingDays(empId, month, year);
    }
}
