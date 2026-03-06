package com.example.qlns.Service;

import com.example.qlns.Entity.*;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// =============================================
// 3. ATTENDANCE SERVICE (TV3 viết)
// =============================================
@Service
public class AttendanceService {

    private static final double OFFICE_LAT  = 10.7769;
    private static final double OFFICE_LNG  = 106.7009;
    private static final double MAX_DISTANCE = 1000.0;  // 1km

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Attendance checkIn(Long employeeId, Double lat, Double lng) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", employeeId));

        // Kiểm tra đã nghỉ việc chưa
        if (employee.getEndDate() != null && !employee.getEndDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Nhân viên này đã nghỉ việc");
        }

        // Kiểm tra hôm nay có đơn nghỉ phép được duyệt không → không cho chấm công
        if (attendanceRepository.existsByEmployeeIdAndDateAndStatus(
                employeeId, LocalDate.now(), Attendance.AttendanceStatus.LEAVE)) {
            throw new AttendanceException("Hôm nay bạn đang trong ngày nghỉ phép");
        }

        // Kiểm tra đã chấm công hôm nay chưa
        if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, LocalDate.now())) {
            throw new AttendanceException("Bạn đã chấm công hôm nay rồi");
        }

        // Kiểm tra GPS
        double distance = haversine(lat, lng, OFFICE_LAT, OFFICE_LNG);
        if (distance > MAX_DISTANCE) {
            throw new LocationException(distance);
        }

        Attendance attendance = new Attendance(employee, LocalDate.now(),
                LocalDateTime.now(), lat, lng, distance);

        // Đánh dấu trễ nếu sau 8:30
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() > 8 || (now.getHour() == 8 && now.getMinute() > 30)) {
            attendance.setStatus(Attendance.AttendanceStatus.LATE);
        }

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance checkOut(Long employeeId) {
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .orElseThrow(() -> new AttendanceException("Bạn chưa chấm công vào hôm nay"));

        if (attendance.getCheckOutTime() != null) {
            throw new AttendanceException("Bạn đã chấm công ra rồi");
        }
        attendance.setCheckOutTime(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employeeId);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public List<Attendance> getByMonth(Long employeeId, int year, int month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, from, to);
    }

    // Haversine formula
    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
