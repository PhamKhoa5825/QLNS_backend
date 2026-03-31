package com.example.qlns.Service;

import com.example.qlns.DTO.Response.SalaryRecordDTO;
import com.example.qlns.Entity.*;
import com.example.qlns.Enum.*;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired
    private SalaryRecordRepository salaryRepo;
    @Autowired
    private EmployeeRepository empRepo;
    @Autowired
    private AttendanceRepository attendanceRepo;
    @Autowired
    private RequestRepository requestRepo;
    @Autowired
    private com.example.qlns.Repository.HolidayRepository holidayRepository;

    private static final int WORK_HOURS_PER_DAY = 8;

    @Transactional
    public SalaryRecordDTO generateForEmployee(Long empId, int month, int year) {
        Employee emp = empRepo.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        salaryRepo.findByEmployeeIdAndMonthAndYear(empId, month, year)
                .filter(r -> r.getStatus() == SalaryRecordStatus.DRAFT)
                .ifPresent(salaryRepo::delete);

        SalaryRecord record = new SalaryRecord();
        record.setEmployee(emp);
        record.setMonth(month);
        record.setYear(year);
        
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        
        int workingDaysStandard = countBusinessDays(from, to);
        record.setWorkingDaysStandard(workingDaysStandard);
        record.setBaseSalary(emp.getBaseSalary());

        List<Attendance> attendances = attendanceRepo.findByEmployeeIdAndDateBetween(empId, from, to);
        List<Request> approvedRequests = requestRepo.findByEmployeeIdAndStatus(empId, RequestStatus.APPROVED);

        // Step 1: Calculate Worked Days (Union of Attendance & Business Trips, minus weekends)
        Set<LocalDate> workedDates = new HashSet<>();
        for (Attendance att : attendances) {
            if (!isWeekend(att.getDate())) workedDates.add(att.getDate());
        }
        for (Request req : approvedRequests) {
            if (req.getType() == RequestType.BUSINESS_TRIP) {
                for (RequestDetail detail : req.getDetails()) {
                    LocalDate d = detail.getSpecificDate();
                    if (d != null && !d.isBefore(from) && !d.isAfter(to) && !isWeekend(d)) {
                        workedDates.add(d);
                    }
                }
            }
        }

        double daysWorkedSum = 0;
        for (LocalDate date : workedDates) {
            // Check for half-day leave on this day
            boolean hasHalfDayLeave = approvedRequests.stream()
                .filter(r -> r.getType() == RequestType.LEAVE_ANNUAL || r.getType() == RequestType.LEAVE_UNPAID || r.getType() == RequestType.SICK_LEAVE)
                .flatMap(r -> r.getDetails().stream())
                .anyMatch(d -> d.getSpecificDate() != null && d.getSpecificDate().equals(date) && d.getLeaveSession() != null && d.getLeaveSession() != LeaveSession.ALL_DAY);
            daysWorkedSum += (hasHalfDayLeave ? 0.5 : 1.0);
        }
        record.setDaysWorked(daysWorkedSum);

        // Step 2: Calculate Excused Absence & Sick Leave & Business Trip Days
        double excusedAbsent = 0;
        double daysSick = 0;
        Set<LocalDate> tripDates = new HashSet<>();
        for (Request req : approvedRequests) {
            if (req.getType() == RequestType.LEAVE_ANNUAL || req.getType() == RequestType.SICK_LEAVE || req.getType() == RequestType.BUSINESS_TRIP) {
                for (RequestDetail detail : req.getDetails()) {
                    LocalDate date = detail.getSpecificDate();
                    if (date != null && date.getYear() == year && date.getMonthValue() == month && !isWeekend(date)) {
                        double value = (detail.getLeaveSession() == LeaveSession.MORNING || detail.getLeaveSession() == LeaveSession.AFTERNOON ? 0.5 : 1.0);
                        if (req.getType() == RequestType.SICK_LEAVE) daysSick += value;
                        else if (req.getType() == RequestType.BUSINESS_TRIP) tripDates.add(date);
                        else if (req.getType() == RequestType.LEAVE_ANNUAL) excusedAbsent += value;
                    }
                }
            }
        }
        // Tính toán các ngày Lễ trong tháng để không bị quy vào Vắng không phép
        List<com.example.qlns.Entity.Holiday> holidays = holidayRepository.findByDateBetweenOrderByDateAsc(from, to);
        double daysHoliday = 0;
        for (com.example.qlns.Entity.Holiday h : holidays) {
            if (!isWeekend(h.getDate())) {
                daysHoliday += 1.0;
            }
        }
        excusedAbsent += daysHoliday;

        record.setDaysAbsentExcused(excusedAbsent); // Now only includes Annual Leave
        record.setDaysSick(daysSick);
        record.setBusinessTripDays((double) tripDates.size());

        double unexcused = Math.max(0, workingDaysStandard - record.getDaysWorked() - excusedAbsent - daysSick);
        record.setDaysAbsentUnexcused(unexcused);

        // Step 3: Deductions (Late & Absence)
        double dailyWage = emp.getBaseSalary() / workingDaysStandard;
        double hourlyWage = dailyWage / WORK_HOURS_PER_DAY;
        
        int totalLateMinutes = attendances.stream().mapToInt(a -> a.getLateMinutes() != null ? a.getLateMinutes() : 0).sum();
        double deductionLate = 0;
        for (Attendance att : attendances) {
            int mins = att.getLateMinutes() != null ? att.getLateMinutes() : 0;
            if (mins >= 15 && mins <= 60) deductionLate += hourlyWage * 0.5;
            else if (mins > 60) deductionLate += hourlyWage * 1.0;
        }
        record.setTotalLateMinutes(totalLateMinutes);
        record.setDeductionLate(Math.round(deductionLate * 100.0) / 100.0);
        record.setDeductionUnexcused(Math.round(unexcused * dailyWage * 100.0) / 100.0);
        record.setDeductionSick(Math.round(daysSick * dailyWage * 100.0) / 100.0);

        // Step 4: Overtime
        Map<LocalDate, Attendance> attendanceMap = attendances.stream().collect(Collectors.toMap(Attendance::getDate, a -> a, (a1, a2) -> a1));
        double totalOTHours = 0;
        double overtimeBonus = 0;
        for (Request req : approvedRequests) {
            if (req.getType() == RequestType.OVERTIME) {
                for (RequestDetail detail : req.getDetails()) {
                    LocalDate date = detail.getSpecificDate();
                    if (date != null && date.getYear() == year && date.getMonthValue() == month) {
                        double reqHours = detail.getOvertimeHours() != null ? detail.getOvertimeHours() : 0;
                        Attendance att = attendanceMap.get(date);
                        if (att != null) {
                            double std = isWeekend(date) ? 0.0 : 8.0;
                            double actualOT = Math.max(0, (att.getWorkHours() != null ? att.getWorkHours() : 0.0) - std);
                            double payable = Math.min(reqHours, actualOT);
                            if (payable > 0) {
                                totalOTHours += payable;
                                overtimeBonus += payable * hourlyWage * (isWeekend(date) ? 2.0 : 1.5);
                            }
                        }
                    }
                }
            }
        }
        record.setTotalOvertimeHours(totalOTHours);
        record.setOvertimeBonus(Math.round(overtimeBonus * 100.0) / 100.0);

        // Step 5: Performance & Final Sum
        double[] perf = calculatePerformance(empId, month, year, record.getDaysWorked(), unexcused, totalLateMinutes, workingDaysStandard);
        record.setPerformanceScore(perf[0]);
        record.setPerformanceGrade(resolveGrade(perf[0]));
        record.setTaskBonus(Math.round(perf[1] * 100.0) / 100.0);

        double gross = emp.getBaseSalary() - record.getDeductionLate() - record.getDeductionUnexcused() - record.getDeductionSick() + record.getTaskBonus() + record.getOvertimeBonus();
        record.setGrossSalary(Math.max(0, Math.round(gross * 100.0) / 100.0));
        record.setStatus(SalaryRecordStatus.DRAFT);

        return SalaryRecordDTO.from(salaryRepo.save(record));
    }

    @Transactional(readOnly = true)
    public SalaryRecordDTO estimateForEmployee(Long empId, int month, int year) {
        Employee emp = empRepo.findById(empId).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        if (emp.getBaseSalary() == null || emp.getBaseSalary() <= 0) {
            SalaryRecord empty = new SalaryRecord();
            empty.setEmployee(emp); empty.setMonth(month); empty.setYear(year); empty.setBaseSalary(0.0);
            return SalaryRecordDTO.from(empty);
        }

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        int workingDaysStandard = countBusinessDays(from, to);
        LocalDate todayOrEnd = (year == LocalDate.now().getYear() && month == LocalDate.now().getMonthValue()) ? LocalDate.now() : to;

        List<Attendance> attendances = attendanceRepo.findByEmployeeIdAndDateBetween(empId, from, todayOrEnd);
        List<Request> approvedRequests = requestRepo.findByEmployeeIdAndStatus(empId, RequestStatus.APPROVED);

        // Calculate daysWorked (Union of Attendance & Trips, minus weekends)
        Set<LocalDate> workedDates = new HashSet<>();
        for (Attendance att : attendances) {
            if (!isWeekend(att.getDate())) workedDates.add(att.getDate());
        }
        for (Request req : approvedRequests) {
            if (req.getType() == RequestType.BUSINESS_TRIP) {
                for (RequestDetail detail : req.getDetails()) {
                    LocalDate d = detail.getSpecificDate();
                    if (d != null && !d.isBefore(from) && !d.isAfter(todayOrEnd) && !isWeekend(d)) {
                        workedDates.add(d);
                    }
                }
            }
        }

        double daysWorkedSum = 0;
        for (LocalDate date : workedDates) {
            boolean hasHalfDayLeave = approvedRequests.stream()
                .filter(r -> r.getType() == RequestType.LEAVE_ANNUAL || r.getType() == RequestType.LEAVE_UNPAID || r.getType() == RequestType.SICK_LEAVE)
                .flatMap(r -> r.getDetails().stream())
                .anyMatch(d -> d.getSpecificDate() != null && d.getSpecificDate().equals(date) && d.getLeaveSession() != null && d.getLeaveSession() != LeaveSession.ALL_DAY);
            daysWorkedSum += (hasHalfDayLeave ? 0.5 : 1.0);
        }

        double excusedAbsent = 0;
        double daysSick = 0;
        Set<LocalDate> tripDates = new HashSet<>();
        for (Request req : approvedRequests) {
            if (req.getType() == RequestType.LEAVE_ANNUAL || req.getType() == RequestType.SICK_LEAVE || req.getType() == RequestType.BUSINESS_TRIP) {
                for (RequestDetail detail : req.getDetails()) {
                    LocalDate date = detail.getSpecificDate();
                    if (date != null && !date.isBefore(from) && !date.isAfter(todayOrEnd) && !isWeekend(date)) {
                        double value = (detail.getLeaveSession() == LeaveSession.MORNING || detail.getLeaveSession() == LeaveSession.AFTERNOON ? 0.5 : 1.0);
                        if (req.getType() == RequestType.SICK_LEAVE) daysSick += value;
                        else if (req.getType() == RequestType.BUSINESS_TRIP) tripDates.add(date);
                        else if (req.getType() == RequestType.LEAVE_ANNUAL) excusedAbsent += value;
                    }
                }
            }
        }

        // Tính toán các ngày Lễ trong phạm vi
        List<com.example.qlns.Entity.Holiday> holidays = holidayRepository.findByDateBetweenOrderByDateAsc(from, todayOrEnd);
        double daysHoliday = 0;
        for (com.example.qlns.Entity.Holiday h : holidays) {
            if (!isWeekend(h.getDate())) {
                daysHoliday += 1.0;
            }
        }
        excusedAbsent += daysHoliday;

        SalaryRecord est = new SalaryRecord();
        est.setEmployee(emp); est.setMonth(month); est.setYear(year);
        est.setWorkingDaysStandard(workingDaysStandard); est.setBaseSalary(emp.getBaseSalary());
        est.setDaysWorked(daysWorkedSum);
        est.setBusinessTripDays((double) tripDates.size());
        est.setDaysAbsentExcused(excusedAbsent);
        est.setDaysSick(daysSick);
        
        int expectedDaysSoFar = countBusinessDays(from, todayOrEnd);
        double unexcused = Math.max(0, expectedDaysSoFar - daysWorkedSum - excusedAbsent - daysSick);
        est.setDaysAbsentUnexcused(unexcused);

        double dailyWage = emp.getBaseSalary() / workingDaysStandard;
        double hourlyWage = dailyWage / WORK_HOURS_PER_DAY;

        int totalLate = attendances.stream().mapToInt(a -> a.getLateMinutes() != null ? a.getLateMinutes() : 0).sum();
        double deductionLate = 0;
        for (Attendance att : attendances) {
            int mins = att.getLateMinutes() != null ? att.getLateMinutes() : 0;
            if (mins >= 15 && mins <= 60) deductionLate += hourlyWage * 0.5;
            else if (mins > 60) deductionLate += hourlyWage * 1.0;
        }
        est.setTotalLateMinutes(totalLate);
        est.setDeductionLate(Math.round(deductionLate * 100.0) / 100.0);
        est.setDeductionUnexcused(Math.round(unexcused * dailyWage * 100.0) / 100.0);
        est.setDeductionSick(Math.round(daysSick * dailyWage * 100.0) / 100.0);

        Map<LocalDate, Attendance> attendanceMap = attendances.stream().collect(Collectors.toMap(Attendance::getDate, a -> a, (a1, a2) -> a1));
        double totalOTHours = 0;
        double overtimeBonus = 0;
        for (Request req : approvedRequests) {
            if (req.getType() == RequestType.OVERTIME) {
                for (RequestDetail detail : req.getDetails()) {
                    LocalDate date = detail.getSpecificDate();
                    if (date != null && !date.isBefore(from) && !date.isAfter(todayOrEnd)) {
                        double reqHours = detail.getOvertimeHours() != null ? detail.getOvertimeHours() : 0;
                        Attendance att = attendanceMap.get(date);
                        if (att != null) {
                            double std = isWeekend(date) ? 0.0 : 8.0;
                            double actualWorkHours = (att.getWorkHours() != null ? (double) att.getWorkHours() : 0.0);
                            double actualOT = Math.max(0, actualWorkHours - std);
                            double payable = Math.min(reqHours, actualOT);
                            if (payable > 0) {
                                totalOTHours += payable;
                                overtimeBonus += payable * hourlyWage * (isWeekend(date) ? 2.0 : 1.5);
                            }
                        }
                    }
                }
            }
        }
        est.setTotalOvertimeHours(totalOTHours);
        est.setOvertimeBonus(Math.round(overtimeBonus * 100.0) / 100.0);

        double[] perf = calculatePerformance(empId, month, year, est.getDaysWorked(), unexcused, totalLate, workingDaysStandard);
        est.setPerformanceScore(perf[0]); est.setPerformanceGrade(resolveGrade(perf[0])); est.setTaskBonus(perf[1]);

        double gross = emp.getBaseSalary() - est.getDeductionLate() - est.getDeductionUnexcused() - est.getDeductionSick() + est.getTaskBonus() + est.getOvertimeBonus();
        est.setGrossSalary(Math.max(0, Math.round(gross * 100.0) / 100.0));
        est.setStatus(SalaryRecordStatus.DRAFT);
        return SalaryRecordDTO.from(est);
    }

    @Transactional(readOnly = true)
    public SalaryRecordDTO getOrEstimate(Long empId, int month, int year) {
        try {
            Optional<SalaryRecord> existing = salaryRepo.findByEmployeeIdAndMonthAndYear(empId, month, year);
            if (existing.isPresent() && existing.get().getStatus() != SalaryRecordStatus.DRAFT) {
                return SalaryRecordDTO.from(existing.get());
            }
            return estimateForEmployee(empId, month, year);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public SalaryRecordDTO getForEmployee(Long empId, int month, int year) {
        return getOrEstimate(empId, month, year);
    }

    public List<SalaryRecordDTO> getHistoryForEmployee(Long empId) {
        return salaryRepo.findByEmployeeId(empId).stream().map(SalaryRecordDTO::from).collect(Collectors.toList());
    }

    public List<SalaryRecordDTO> generateForDepartment(Long deptId, int month, int year) {
        return empRepo.findByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE).stream()
                .map(emp -> generateForEmployee(emp.getId(), month, year)).collect(Collectors.toList());
    }

    public List<SalaryRecordDTO> generateForAll(int month, int year) {
        return empRepo.findAll().stream().filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .map(emp -> generateForEmployee(emp.getId(), month, year)).collect(Collectors.toList());
    }

    public List<SalaryRecordDTO> getForDepartment(Long deptId, int month, int year) {
        return salaryRepo.findByDepartmentAndMonthAndYear(deptId, month, year).stream().map(SalaryRecordDTO::from).collect(Collectors.toList());
    }

    public List<SalaryRecordDTO> getAll(int month, int year) {
        return salaryRepo.findByMonthAndYear(month, year).stream().map(SalaryRecordDTO::from).collect(Collectors.toList());
    }

    public List<SalaryRecordDTO> getSummaryForAll(int month, int year) {
        return empRepo.findAll().stream().filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .map(emp -> getOrEstimate(emp.getId(), month, year)).collect(Collectors.toList());
    }

    @Transactional
    public SalaryRecordDTO finalize(Long id) {
        SalaryRecord record = salaryRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        record.setStatus(SalaryRecordStatus.FINALIZED);
        return SalaryRecordDTO.from(salaryRepo.save(record));
    }

    @Transactional
    public SalaryRecordDTO addNote(Long recordId, String note) {
        SalaryRecord r = salaryRepo.findById(recordId).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        r.setNote(note);
        return SalaryRecordDTO.from(salaryRepo.save(r));
    }

    @Transactional
    public void updateBaseSalary(Long empId, Double salary) {
        Employee emp = empRepo.findById(empId).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        emp.setBaseSalary(salary);
        empRepo.save(emp);
    }

    private boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }

    private int countBusinessDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            if (!isWeekend(cur)) count++;
            cur = cur.plusDays(1);
        }
        return count;
    }

    private double[] calculatePerformance(Long empId, int month, int year, double daysWorked, double unexcused, int lateMins, int workingDaysStd) {
        double score = 100.0;

        // 1. Penalty for Unexcused Absences (Heavy: 20 points per day)
        // This ensures 1 day unexcused (80 pts) disqualifies from both 95 and 85 bonus tiers.
        score -= (unexcused * 20.0);

        // 2. Penalty for Tardiness
        if (lateMins > 120) score -= 20;
        else if (lateMins > 60) score -= 10;
        else if (lateMins > 30) score -= 5;

        score = Math.max(0, Math.min(100.0, score));

        double bonus = 0;
        if (score >= 95) bonus = 500000;      // Top tier
        else if (score >= 85) bonus = 200000; // Second tier

        return new double[]{score, bonus};
    }

    private String resolveGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 75) return "B";
        if (score >= 45) return "C";
        return "D";
    }
}
