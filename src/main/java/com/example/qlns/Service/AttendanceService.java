package com.example.qlns.Service;

import com.example.qlns.DTO.Response.AttendanceDTO;
import com.example.qlns.DTO.Response.AttendanceStatsDTO;
import com.example.qlns.DTO.Response.AttendanceSummaryDTO;
import com.example.qlns.DTO.Response.EmployeeAttendanceStatsDTO;
import com.example.qlns.Entity.Attendance;
import com.example.qlns.Entity.CompanySettings;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.AttendanceStatus;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Exception.AttendanceException;
import com.example.qlns.Exception.LocationException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.AttendanceRepository;
import com.example.qlns.Repository.CompanySettingsRepository;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.RequestRepository;
import com.example.qlns.Entity.Request;
import com.example.qlns.Enum.RequestStatus;
import com.example.qlns.Enum.RequestType;
import com.example.qlns.DTO.Response.AttendanceMonthlyResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepo;
    @Autowired
    private EmployeeRepository empRepo;
    @Autowired
    private CompanySettingsRepository settingsRepo;
    @Autowired
    private RequestRepository requestRepo;
    @Autowired
    private com.example.qlns.Repository.HolidayRepository holidayRepository;
    @Autowired
    private com.example.qlns.Repository.UserRepository userRepository;
    @Autowired
    private SystemLogService logService;

    private static final int WORKING_DAYS_STANDARD = 22;
    private static final int WORK_HOURS_PER_DAY = 8;

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private CompanySettings getSettings() {
        return settingsRepo.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa cấu hình công ty"));
    }

    /**
     * Tìm đơn nghỉ nửa ngày đã duyệt cho nhân viên vào ngày cụ thể.
     * Trả về LeaveSession nếu tìm thấy, null nếu không.
     */
    private com.example.qlns.Enum.LeaveSession getApprovedLeaveSession(Long employeeId, LocalDate date) {
        List<com.example.qlns.Entity.Request> approvedRequests = requestRepo.findByEmployeeIdAndStatus(employeeId,
                com.example.qlns.Enum.RequestStatus.APPROVED);
        for (com.example.qlns.Entity.Request req : approvedRequests) {
            if (req.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL
                    || req.getType() == com.example.qlns.Enum.RequestType.LEAVE_UNPAID
                    || req.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE) {
                for (com.example.qlns.Entity.RequestDetail detail : req.getDetails()) {
                    if (detail.getSpecificDate() != null && detail.getSpecificDate().equals(date)) {
                        return detail.getLeaveSession();
                    }
                }
            }
        }
        return null;
    }

    @Transactional
    public AttendanceDTO checkIn(Long employeeId, Double lat, Double lng) {
        Employee emp = empRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        LocalDate today = LocalDate.now();
        if (attendanceRepo.existsByEmployeeIdAndDate(employeeId, today))
            throw new AttendanceException("Đã chấm công hôm nay rồi");

        // Khóa chấm công ngày lễ 
        if (holidayRepository.existsByDate(today)) {
            boolean hasOT = requestRepo.findByEmployeeIdAndStatus(employeeId, RequestStatus.APPROVED)
                    .stream()
                    .anyMatch(r -> r.getType() == RequestType.OVERTIME && 
                            r.getDetails().stream().anyMatch(d -> d.getSpecificDate() != null && d.getSpecificDate().equals(today)));
            if (!hasOT) {
                throw new AttendanceException("Hôm nay là Ngày Lễ, bạn không cần chấm công (yêu cầu điền đơn Phê duyệt OT nếu có lịch làm việc).");
            }
        }

        CompanySettings settings = getSettings();

        // GPS Check (from Employee App)
        if (lat != null && lng != null) {
            double distance = haversine(lat, lng, settings.getBaseLat(), settings.getBaseLng());
            if (distance > settings.getAllowedRadius())
                throw new LocationException(String.format(
                        "Ngoài phạm vi %dm (bạn cách văn phòng %.0fm)",
                        settings.getAllowedRadius(), distance));
        }

        // Xác định giờ bắt đầu kỳ vọng dựa trên đơn nghỉ nửa ngày
        com.example.qlns.Enum.LeaveSession leaveSession = getApprovedLeaveSession(employeeId, today);
        LocalTime expectedStart;
        if (leaveSession == com.example.qlns.Enum.LeaveSession.MORNING) {
            // Nghỉ buổi sáng → kỳ vọng check-in lúc đầu ca chiều
            expectedStart = LocalTime.parse(settings.getAfternoonStartTime());
        } else if (leaveSession == com.example.qlns.Enum.LeaveSession.ALL_DAY) {
            // Nghỉ cả ngày → không cần chấm công, nhưng nếu vẫn chấm thì tính bình thường
            expectedStart = LocalTime.parse(settings.getWorkStartTime());
        } else {
            // Không nghỉ hoặc nghỉ buổi chiều → check-in bình thường đầu ca sáng
            expectedStart = LocalTime.parse(settings.getWorkStartTime());
        }

        LocalTime nowTime = LocalTime.now();
        Integer lateMinutes = 0;
        AttendanceStatus status = AttendanceStatus.ON_TIME;

        // --- Weekend OT Exception: No 'Late' on weekends ---
        if (!isWeekend(today) && nowTime.isAfter(expectedStart)) {
            lateMinutes = (int) Duration.between(expectedStart, nowTime).toMinutes();
            status = AttendanceStatus.LATE;
        }

        Attendance att = new Attendance();
        att.setEmployee(emp);
        att.setDate(today);
        att.setCheckIn(LocalDateTime.now());
        att.setLocationLat(lat);
        att.setLocationLng(lng);
        att.setStatus(status);
        att.setLateMinutes(lateMinutes);

        Attendance saved = attendanceRepo.save(att);
        userRepository.findByEmployeeId(employeeId).ifPresent(u -> 
            logService.log(u, "CREATE", "Nhân viên " + emp.getFullName() + " đã chấm công vào lúc " + att.getCheckIn().toLocalTime().toString().substring(0, 5))
        );
        return AttendanceDTO.from(saved);
    }

    @Transactional
    public AttendanceDTO checkOut(Long employeeId) {
        Attendance att = attendanceRepo
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .orElseThrow(() -> new AttendanceException("Bạn chưa check-in hôm nay"));

        if (att.getCheckOut() != null)
            throw new AttendanceException("Bạn đã check-out rồi");

        CompanySettings settings = getSettings();

        // Xác định giờ kết thúc kỳ vọng dựa trên đơn nghỉ nửa ngày
        com.example.qlns.Enum.LeaveSession leaveSession = getApprovedLeaveSession(employeeId, LocalDate.now());
        LocalTime expectedEnd;
        if (leaveSession == com.example.qlns.Enum.LeaveSession.AFTERNOON) {
            // Nghỉ buổi chiều → cho phép check-out lúc kết thúc ca sáng
            expectedEnd = LocalTime.parse(settings.getMorningEndTime());
        } else {
            // Bình thường → check-out cuối ngày
            expectedEnd = LocalTime.parse(settings.getWorkEndTime());
        }

        if (!isWeekend(LocalDate.now()) && LocalTime.now().isBefore(expectedEnd))
            throw new AttendanceException(
                    "Chưa đến giờ check-out. Giờ tan ca: " + expectedEnd);

        LocalDateTime checkOut = LocalDateTime.now();
        double workHours = calculateNetWorkHours(att.getCheckIn(), checkOut, settings);

        att.setCheckOut(checkOut);
        att.setWorkHours((float) workHours);

        Attendance saved = attendanceRepo.save(att);
        userRepository.findByEmployeeId(employeeId).ifPresent(u -> 
            logService.log(u, "UPDATE", "Nhân viên " + att.getEmployee().getFullName() + " đã chấm công ra lúc " + att.getCheckOut().toLocalTime().toString().substring(0, 5))
        );
        return AttendanceDTO.from(saved);
    }

    /**
     * Tính tổng số giờ làm việc thực tế, loại trừ giờ nghỉ trưa (thường là 12:00 - 13:00)
     */
    public double calculateNetWorkHours(LocalDateTime start, LocalDateTime end, CompanySettings settings) {
        if (start == null || end == null || end.isBefore(start)) return 0.0;

        LocalTime morningEnd = LocalTime.parse(settings.getMorningEndTime());
        LocalTime afternoonStart = LocalTime.parse(settings.getAfternoonStartTime());

        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        // Khối ca sáng: từ lúc check-in đến min(check-out, 12:00)
        long morningMinutes = 0;
        if (startTime.isBefore(morningEnd)) {
            LocalTime effectiveEnd = endTime.isBefore(morningEnd) ? endTime : morningEnd;
            if (effectiveEnd.isAfter(startTime)) {
                morningMinutes = Duration.between(startTime, effectiveEnd).toMinutes();
            }
        }

        // Khối ca chiều: từ lúc max(check-in, 13:00) đến lúc check-out
        long afternoonMinutes = 0;
        if (endTime.isAfter(afternoonStart)) {
            LocalTime effectiveStart = startTime.isAfter(afternoonStart) ? startTime : afternoonStart;
            if (endTime.isAfter(effectiveStart)) {
                afternoonMinutes = Duration.between(effectiveStart, endTime).toMinutes();
            }
        }

        return (morningMinutes + afternoonMinutes) / 60.0;
    }

    public List<Attendance> getByEmployee(Long empId) {
        return attendanceRepo.findByEmployeeId(empId);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepo.findByDate(date);
    }

    public List<Attendance> getByDateAndDepartment(LocalDate date, Long deptId) {
        return attendanceRepo.findByDateAndEmployeeDepartmentId(date, deptId);
    }

    public Attendance getTodayAttendance(Long employeeId) {
        return attendanceRepo.findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .orElse(null);
    }

    public List<Attendance> getByEmployeeAndMonth(Long empId, int month, int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return attendanceRepo.findByEmployeeIdAndDateBetween(empId, from, to);
    }

    /**
     * Lấy thống kê tổng hợp cho Dashboard (Manager/Employee)
     */
    public AttendanceStatsDTO getMonthlyStats(Long empId, int month, int year) {
        List<Attendance> attendances = getByEmployeeAndMonth(empId, month, year);

        double totalHours = attendances.stream()
                .filter(a -> a.getWorkHours() != null)
                .mapToDouble(a -> (double) a.getWorkHours())
                .sum();

        long onTime = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_TIME)
                .count();

        long late = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        // Absent logic: 22 working days minus (onTime + late)
        long absent = Math.max(0, 22 - (onTime + late));

        totalHours = Math.round(totalHours * 100.0) / 100.0;
        double avgHours = attendances.isEmpty() ? 0 : totalHours / attendances.size();
        avgHours = Math.round(avgHours * 100.0) / 100.0;

        return new AttendanceStatsDTO(totalHours, onTime, late, absent, avgHours);
    }

    public List<EmployeeAttendanceStatsDTO> getDepartmentStats(Long deptId, int month, int year) {
        List<Employee> employees = empRepo.findByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE);
        return employees.stream().map(emp -> {
            long onTime = attendanceRepo.countByEmployeeAndStatus(emp.getId(), month, year, AttendanceStatus.ON_TIME);
            long late = attendanceRepo.countByEmployeeAndStatus(emp.getId(), month, year, AttendanceStatus.LATE);
            long absent = Math.max(0, 22 - (onTime + late));
            return new EmployeeAttendanceStatsDTO(emp.getId(), emp.getFullName(), onTime, late, absent);
        }).collect(Collectors.toList());
    }

    public long countWorkingDays(Long empId, int month, int year) {
        return attendanceRepo.countWorkingDays(empId, month, year);
    }

    @Transactional(readOnly = true)
    public AttendanceMonthlyResponseDTO getMonthlySummary(Long empId, int month, int year) {
        Employee emp = empRepo.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        LocalDate from = LocalDate.of(year, month, 1);
        int daysInMonth = from.lengthOfMonth();
        List<AttendanceSummaryDTO> summaryList = new ArrayList<>();

        List<Attendance> attendances = attendanceRepo.findByEmployeeIdAndDateBetween(empId, from,
                from.plusDays(daysInMonth - 1));
        List<Request> approvedRequests = requestRepo.findByEmployeeIdAndStatus(empId, RequestStatus.APPROVED);
        List<com.example.qlns.Entity.Holiday> holidays = holidayRepository.findByDateBetweenOrderByDateAsc(from, from.plusDays(daysInMonth - 1));
        CompanySettings settings = getSettings();

        double baseSal = emp.getBaseSalary() != null ? emp.getBaseSalary() : 0.0;
        double dailyWage = baseSal / WORKING_DAYS_STANDARD;
        double hourlyWage = dailyWage / WORK_HOURS_PER_DAY;

        double totalDeductions = 0;
        double totalBonuses = 0;
        double totalSalaryImpact = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = LocalDate.of(year, month, i);
            Attendance att = attendances.stream()
                    .filter(a -> a.getDate().equals(date))
                    .findFirst().orElse(null);

            // Đảm bảo số giờ làm việc trong báo cáo luôn được trừ nghỉ trưa (kể cả dữ liệu cũ)
            if (att != null && att.getCheckIn() != null && att.getCheckOut() != null) {
                double netHours = calculateNetWorkHours(att.getCheckIn(), att.getCheckOut(), settings);
                att.setWorkHours((float) netHours);
            }

            AttendanceSummaryDTO daySummary = calculateDailySummary(date, att, approvedRequests, holidays);

            // Calculate financial impact for this day
            double impact = 0;
            boolean isPaid = false;
            double dayValueDouble = 0.0; // Default to 0, will be set based on status

            String status = daySummary.getStatus();
            if ("PRESENT".equals(status) || "PRESENT_PARTIAL".equals(status)) {
                impact = dailyWage;
                isPaid = true;
                dayValueDouble = 1.0;
            } else if ("LATE".equals(status) || "LATE_PARTIAL".equals(status)) {
                int mins = att != null && att.getLateMinutes() != null ? att.getLateMinutes() : 0;
                double deduction = 0;
                if (mins >= 15 && mins <= 60)
                    deduction = hourlyWage * 0.5;
                else if (mins > 60)
                    deduction = hourlyWage * 1.0;

                impact = dailyWage - deduction;
                totalDeductions += deduction;
                isPaid = true;
                dayValueDouble = 1.0; 
            } else if ("HOLIDAY".equals(status) || "LEAVE_ANNUAL".equals(status) || "LEAVE_ANNUAL_PARTIAL".equals(status)) {
                impact = dailyWage;
                isPaid = true;
                dayValueDouble = 1.0;
            } else if ("LEAVE_UNPAID".equals(status) || "SICK_LEAVE".equals(status)) {
                impact = 0;
                isPaid = false;
                totalDeductions += dailyWage;
                dayValueDouble = 0.0;
            } else if ("LEAVE_UNPAID_PARTIAL".equals(status) || "SICK_LEAVE_PARTIAL".equals(status)) {
                double workPart = (att != null ? dailyWage * 0.5 : 0.0);
                impact = workPart;
                totalDeductions += dailyWage * 0.5;
                isPaid = (att != null);
                dayValueDouble = (att != null ? 0.5 : 0.0);
            } else if ("TRIP".equals(status)) {
                impact = dailyWage;
                isPaid = true;
                dayValueDouble = 1.0;
            } else if ("ABSENT".equals(status) && !isWeekend(date)) {
                impact = 0;
                isPaid = false;
                totalDeductions += dailyWage;
                dayValueDouble = 0.0;
            } else if ("WEEKEND".equals(status) || "OT".equals(status)) {
                impact = 0;
                isPaid = false;
                dayValueDouble = 0.0;
            }

            // --- Independent Overtime Calculation (Applies to all days) ---
            final LocalDate currentDate = date;
            Request otReq = approvedRequests.stream()
                    .filter(r -> r.getType() == RequestType.OVERTIME)
                    .filter(r -> r.getDetails().stream().anyMatch(d -> currentDate.equals(d.getSpecificDate())))
                    .findFirst().orElse(null);

            if (otReq != null && att != null) {
                double requestedHours = otReq.getDetails().stream()
                        .filter(d -> currentDate.equals(d.getSpecificDate()))
                        .mapToDouble(d -> d.getOvertimeHours() != null ? d.getOvertimeHours() : 0)
                        .sum();
                
                // --- Strict Validation: Only pay for ACTUAL hours worked beyond standard ---
                double standardHours = isWeekend(date) ? 0.0 : 8.0;
                double actualWorkHours = (att.getWorkHours() != null ? att.getWorkHours() : 0.0);
                double actualOT = Math.max(0, actualWorkHours - standardHours);
                double payableHours = Math.min(requestedHours, actualOT);
                
                if (payableHours > 0) {
                    double multiplier = isWeekend(date) ? 2.0 : 1.5;
                    double bonus = payableHours * hourlyWage * multiplier;
                    impact += bonus;
                    totalBonuses += bonus;
                }
            }
                // If it was just OT on a weekend/absent day, ensure it's marked as paid for display
                if ("OT".equals(status) || "WEEKEND".equals(status)) {
                   isPaid = true;
                }
            // -------------------------------------------------------------


            daySummary.setSalaryImpact(Math.round(impact * 100.0) / 100.0);
            daySummary.setIsPaid(isPaid);
            daySummary.setDayValue(dayValueDouble);
            totalSalaryImpact += impact;
            summaryList.add(daySummary);
        }

        AttendanceMonthlyResponseDTO response = new AttendanceMonthlyResponseDTO();
        response.setDays(summaryList);
        response.setBaseSalary(baseSal);
        response.setEstimatedSalarySoFar(Math.round(totalSalaryImpact * 100.0) / 100.0);
        response.setTotalDeductions(Math.round(totalDeductions * 100.0) / 100.0);
        response.setTotalBonuses(Math.round(totalBonuses * 100.0) / 100.0);
        response.setStandardWorkingDays(WORKING_DAYS_STANDARD);

        return response;
    }

    private AttendanceSummaryDTO calculateDailySummary(LocalDate date, Attendance att, List<Request> requests, List<com.example.qlns.Entity.Holiday> holidays) {

        com.example.qlns.Entity.Holiday holiday = holidays.stream()
                .filter(h -> h.getDate().equals(date))
                .findFirst().orElse(null);
                
        if (holiday != null) {
            AttendanceSummaryDTO dto = new AttendanceSummaryDTO();
            dto.setDate(date.toString());
            dto.setStatus("HOLIDAY");
            dto.setDescription(holiday.getName());
            dto.setColorCode("#E91E63"); // Màu hồng dành cho Lễ Tết
            return dto;
        }

        // Find requests for this specific date
        List<com.example.qlns.Entity.Request> dailyRequests = requests.stream()
                .filter(r -> r.getDetails().stream()
                        .anyMatch(d -> d.getSpecificDate() != null && d.getSpecificDate().equals(date)))
                .collect(Collectors.toList());

        String status = "ABSENT";
        String description = "";
        String color = "#757575"; // Grey for absent

        // Pre-check for Overtime
        boolean hasOT = dailyRequests.stream().anyMatch(r -> r.getType() == com.example.qlns.Enum.RequestType.OVERTIME);

        // 1. Check for Business Trip (Blue) - Priority High
        boolean isTrip = dailyRequests.stream()
                .anyMatch(r -> r.getType() == com.example.qlns.Enum.RequestType.BUSINESS_TRIP);
        if (isTrip) {
            status = "TRIP";
            description = "Công tác";
            color = "#2196F3";
        }
        // 2. Check for Leave (Orange)
        else {
            com.example.qlns.Entity.Request leaveReq = dailyRequests.stream()
                    .filter(r -> r.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL
                            || r.getType() == com.example.qlns.Enum.RequestType.LEAVE_UNPAID
                            || r.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE)
                    .findFirst().orElse(null);

            if (leaveReq != null) {
                com.example.qlns.Enum.LeaveSession session = leaveReq.getDetails().stream()
                        .filter(d -> d.getSpecificDate() != null && d.getSpecificDate().equals(date))
                        .map(d -> d.getLeaveSession())
                        .filter(ls -> ls != null)
                        .findFirst().orElse(com.example.qlns.Enum.LeaveSession.ALL_DAY);

                if (session == com.example.qlns.Enum.LeaveSession.ALL_DAY) {
                    status = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "LEAVE_ANNUAL" : "LEAVE_UNPAID";
                    String leaveTypeName = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "Nghỉ phép năm" : "Nghỉ không lương";
                    if (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE) {
                        status = "SICK_LEAVE";
                        description = "Nghỉ ốm (cả ngày)";
                        color = "#00BCD4";
                    } else {
                        description = leaveTypeName + " (cả ngày)";
                        color = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "#FF9800" : "#FDD835";
                    }
                } else {
                    String leaveTypeName = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "Nghỉ phép năm" : 
                                         (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE ? "Nghỉ ốm" : "Nghỉ không lương");
                    String sessionName = (session == com.example.qlns.Enum.LeaveSession.MORNING ? "sáng" : "chiều");
                    
                    description = leaveTypeName + " (" + sessionName + ")";
                    color = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "#FF9800" : 
                            (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE ? "#00BCD4" : "#FDD835");
                            
                    if (att != null) {
                        status = (att.getStatus() == AttendanceStatus.LATE ? "LATE_PARTIAL" : "PRESENT_PARTIAL");
                        String workStatus = (att.getStatus() == AttendanceStatus.LATE ? "Trễ " + att.getLateMinutes() + "p" : "Đúng giờ");
                        description += " + Đi làm (" + workStatus + ")";
                    } else {
                        status = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "LEAVE_ANNUAL_PARTIAL" : 
                                 (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE ? "SICK_LEAVE_PARTIAL" : "LEAVE_UNPAID_PARTIAL");
                    }
                }
            } else {
                // Calculate OT validation for both weekends and weekdays
                double requestedOTHours = dailyRequests.stream()
                        .filter(r -> r.getType() == com.example.qlns.Enum.RequestType.OVERTIME)
                        .flatMap(r -> r.getDetails().stream())
                        .filter(d -> date.equals(d.getSpecificDate()))
                        .mapToDouble(d -> d.getOvertimeHours() != null ? d.getOvertimeHours() : 0)
                        .sum();

                double actualWorkHoursForOT = (att != null && att.getWorkHours() != null ? att.getWorkHours() : 0.0);
                boolean isOTValidated = (hasOT && att != null && actualWorkHoursForOT >= (isWeekend(date) ? 0 : 8.0) + requestedOTHours - 0.25);

                if (isWeekend(date)) {
                    status = isOTValidated ? "OT" : "WEEKEND";
                    description = isOTValidated ? "Làm thêm ngày nghỉ" : 
                                  (hasOT ? "Làm thêm (Thiếu giờ/vân tay)" : "Cuối tuần");
                    color = isOTValidated ? "#9C27B0" : 
                            (hasOT ? "#CE93D8" : "#BDBDBD");
                } else if (att != null) {
                    status = isOTValidated ? "OT" : (att.getStatus() == AttendanceStatus.LATE ? "LATE" : "PRESENT");
                    description = isOTValidated ? "Làm thêm (Vắng chính)" : 
                                  (att.getStatus() == AttendanceStatus.LATE ? "Trễ " + att.getLateMinutes() + "m" : "Đúng giờ");
                    color = isOTValidated ? "#9C27B0" : (att.getStatus() == AttendanceStatus.LATE ? "#F44336" : "#4CAF50");
                } else {
                    status = isOTValidated ? "OT" : "ABSENT";
                    description = isOTValidated ? "Làm thêm (Vắng chính)" : 
                                  (hasOT ? "Làm thêm (Thiếu giờ/vân tay)" : "Vắng mặt");
                    color = isOTValidated ? "#9C27B0" : 
                            (hasOT ? "#CE93D8" : "#757575");
                }
            }
        }


        
        StringBuilder sb = new StringBuilder(description);
        dailyRequests.stream()
                .filter(r -> r.getType() == com.example.qlns.Enum.RequestType.OVERTIME)
                .findFirst().ifPresent(ot -> {
                    double hours = ot.getDetails().stream()
                            .filter(d -> d.getSpecificDate().equals(date))
                            .mapToDouble(d -> d.getOvertimeHours() != null ? d.getOvertimeHours() : 0.0)
                            .sum();
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append("OT ").append(hours).append("h");
                });

        AttendanceSummaryDTO daySummary = new AttendanceSummaryDTO(date.toString(), status, sb.toString(), color);
        
        // 3-Way Split Logic
        if (isTrip) {
            daySummary.setColorCode("#2196F3"); // Solid Blue for Trip
        } else {
            String leaveColor = null;
            String workColor = null;
            String currentOTColor = null;
            if (hasOT) {
                double requestedOTForSplit = dailyRequests.stream()
                    .filter(r -> r.getType() == com.example.qlns.Enum.RequestType.OVERTIME)
                    .flatMap(r -> r.getDetails().stream())
                    .filter(d -> date.equals(d.getSpecificDate()))
                    .mapToDouble(d -> d.getOvertimeHours() != null ? d.getOvertimeHours() : 0)
                    .sum();
                
                double actualWorkHoursForSplit = (att != null && att.getWorkHours() != null ? att.getWorkHours() : 0.0);
                boolean isOTValidatedForSplit = (att != null && actualWorkHoursForSplit >= (isWeekend(date) ? 0 : 8.0) + requestedOTForSplit - 0.25);
                currentOTColor = isOTValidatedForSplit ? "#9C27B0" : "#CE93D8";
            }

            com.example.qlns.Entity.Request leaveReq = dailyRequests.stream()
                    .filter(r -> r.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL 
                            || r.getType() == com.example.qlns.Enum.RequestType.LEAVE_UNPAID 
                            || r.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE)
                    .findFirst().orElse(null);

            if (leaveReq != null) {
                com.example.qlns.Enum.LeaveSession session = leaveReq.getDetails().stream()
                        .filter(d -> d.getSpecificDate() != null && d.getSpecificDate().equals(date))
                        .map(d -> d.getLeaveSession())
                        .filter(ls -> ls != null)
                        .findFirst().orElse(com.example.qlns.Enum.LeaveSession.ALL_DAY);

                if (session == com.example.qlns.Enum.LeaveSession.ALL_DAY) {
                    leaveColor = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "#FF9800" : 
                                 (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE ? "#00BCD4" : "#FDD835");
                } else if (session == com.example.qlns.Enum.LeaveSession.MORNING) {
                    leaveColor = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "#FF9800" : 
                                 (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE ? "#00BCD4" : "#FDD835");
                    workColor = (att != null ? (att.getStatus() == AttendanceStatus.LATE ? "#F44336" : "#4CAF50") : "#757575");
                } else { // AFTERNOON
                    workColor = (att != null ? (att.getStatus() == AttendanceStatus.LATE ? "#F44336" : "#4CAF50") : "#757575");
                    leaveColor = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "#FF9800" : 
                                 (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE ? "#00BCD4" : "#FDD835");
                }
            } else if (!isWeekend(date)) {
                // Regular working day (Mon-Fri) background
                workColor = (att != null ? (att.getStatus() == AttendanceStatus.LATE ? "#F44336" : "#4CAF50") : "#757575");
            } else {
                // Weekend background (always grey)
                workColor = "#BDBDBD";
            }

            List<String> activeColors = new ArrayList<>();
            if (leaveColor != null) activeColors.add(leaveColor);
            if (workColor != null) activeColors.add(workColor);
            
            // 3. Update Border & Description
            // Highlight current date with a blue border
            if (date.equals(java.time.LocalDate.now())) {
                daySummary.setBorderColor("#2196F3"); // Material Blue for Today
                daySummary.setIsBold(true);
            }

            // Handle description for "Worked during Leave" case (Green border removed)
            if (leaveReq != null && att != null) {
                com.example.qlns.Enum.LeaveSession currentSession = leaveReq.getDetails().stream()
                        .filter(d -> d.getSpecificDate() != null && d.getSpecificDate().equals(date))
                        .map(d -> d.getLeaveSession())
                        .filter(ls -> ls != null)
                        .findFirst().orElse(com.example.qlns.Enum.LeaveSession.ALL_DAY);

                String lateInfo = (att.getStatus() == AttendanceStatus.LATE ? "Trễ " + att.getLateMinutes() + "p" : "Đúng giờ");
                String leaveTypeName = (leaveReq.getType() == com.example.qlns.Enum.RequestType.LEAVE_ANNUAL) ? "Nghỉ phép năm" : 
                                     (leaveReq.getType() == com.example.qlns.Enum.RequestType.SICK_LEAVE ? "Nghỉ ốm" : "Nghỉ không lương");
                String sessionName = (currentSession == com.example.qlns.Enum.LeaveSession.MORNING ? "sáng" : 
                                     (currentSession == com.example.qlns.Enum.LeaveSession.AFTERNOON ? "chiều" : "cả ngày"));
                
                String newDesc = leaveTypeName + " (" + sessionName + ") + Đi làm (" + lateInfo + ")";
                // Preserve OT info if exists in the original sb
                if (sb.toString().contains("OT")) {
                    String otInfo = sb.toString().substring(sb.toString().indexOf("|") != -1 ? sb.toString().indexOf("|") : 0);
                    newDesc += " " + otInfo;
                }
                daySummary.setDescription(newDesc);
            }
            
            // OT border (overrides others if present)
            if (currentOTColor != null) {
                daySummary.setBorderColor(currentOTColor);
            }

            if (activeColors.size() == 1) {
                daySummary.setColorCode(activeColors.get(0));
            } else if (activeColors.size() == 2) {
                daySummary.setTopColor(activeColors.get(0));
                daySummary.setBottomColor(activeColors.get(1));
            }
        }
        return daySummary;
    }

    private boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek dow = date.getDayOfWeek();
        return dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY;
    }
}