package com.example.qlns.Service;


import com.example.qlns.DTO.Request.ReviewRequestRequest;
import com.example.qlns.DTO.Response.RequestDTO;
import com.example.qlns.DTO.Response.RequestDetailDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.Request;
import com.example.qlns.Entity.RequestDetail;
import com.example.qlns.Enum.RequestStatus;
import com.example.qlns.Enum.RequestType;
import com.example.qlns.Enum.LeaveSession;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.RequestDetailRepository;
import com.example.qlns.Repository.RequestRepository;
import com.example.qlns.Repository.AttendanceRepository;
import com.example.qlns.Repository.CompanySettingsRepository;
import com.example.qlns.Entity.Attendance;
import com.example.qlns.Entity.CompanySettings;
import com.example.qlns.Enum.AttendanceStatus;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.example.qlns.Entity.User;
import com.example.qlns.Enum.Role;
import com.example.qlns.Repository.UserRepository;
import java.util.ArrayList;

@Service
public class RequestService {

    @Autowired private RequestRepository requestRepo;
    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private RequestDetailRepository detailRepo;
    @Autowired private AttendanceRepository attendanceRepo;
    @Autowired private AttendanceService attendanceService;
    @Autowired private CompanySettingsRepository settingsRepo;
    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeService employeeService;
    @Autowired private SystemLogService logService;

    @Transactional(readOnly = true)
    public List<RequestDTO> getMyRequests(Long employeeId, Integer month, Integer year) {
        return requestRepo.findByEmployeeIdFiltered(employeeId, month, year)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    @Transactional
    public RequestDTO createRequest(Long employeeId, com.example.qlns.DTO.Request.CreateRequestRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank())
            throw new BadRequestException("Tiêu đề đơn không được để trống");

        Employee emp = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        // Chặn trùng đơn nghỉ & Kiểm tra quỹ phép
        if (req.getType() == RequestType.LEAVE_ANNUAL || req.getType() == RequestType.LEAVE_UNPAID || req.getType() == RequestType.SICK_LEAVE) {
            if (req.getDetails() != null) {
                // 1. Kiểm tra quỹ phép (chỉ cho LEAVE_ANNUAL)
                if (req.getType() == RequestType.LEAVE_ANNUAL) {
                    double requestedDays = calculateLeaveDuration(req.getDetails());
                    double quota = emp.getAnnualLeaveQuota() != null ? emp.getAnnualLeaveQuota() : 12.0;
                    double used = emp.getLeaveDaysUsed() != null ? emp.getLeaveDaysUsed() : 0.0;
                    double remaining = quota - used;
                    if (requestedDays > remaining) {
                        throw new BadRequestException("Lỗi quỹ phép: Bạn không đủ ngày phép (Yêu cầu: " + requestedDays + ", Còn lại: " + remaining + ")");
                    }
                }

                // 2. Kiểm tra trùng
                for (RequestDetailDTO d : req.getDetails()) {
                    if (detailRepo.existsOverlappingLeave(employeeId, d.getSpecificDate(), d.getLeaveSession(), List.of(RequestStatus.PENDING, RequestStatus.APPROVED))) {
                        String sessionStr = "";
                        if (d.getLeaveSession() == LeaveSession.MORNING) sessionStr = " Sáng";
                        else if (d.getLeaveSession() == LeaveSession.AFTERNOON) sessionStr = " Chiều";
                        
                        throw new BadRequestException("Lỗi trùng lịch: Bạn đã có đơn nghỉ trùng vào " + sessionStr.trim() + " ngày " + d.getSpecificDate() + ". Vui lòng kiểm tra lại.");
                    }
                }
            }
        }

        RequestType type = req.getType() != null ? req.getType() : RequestType.LEAVE_ANNUAL;

        // === VALIDATION ===
        if (req.getDetails() != null && !req.getDetails().isEmpty()) {
            // 1. Max 3 ngày nghỉ (áp dụng cho LEAVE_ANNUAL, LEAVE_UNPAID và SICK_LEAVE)
            if ((type == RequestType.LEAVE_ANNUAL || type == RequestType.LEAVE_UNPAID || type == RequestType.SICK_LEAVE)
                    && req.getDetails().size() > 3) {
                throw new BadRequestException("Đơn nghỉ phép tối đa chỉ được 3 ngày");
            }

            // 2. Ngày phải >= hôm nay (trừ SICK_LEAVE và PUNCH_CORRECTION cho phép ngày quá khứ)
            if (type != RequestType.SICK_LEAVE && type != RequestType.PUNCH_CORRECTION) {
                java.time.LocalDate today = java.time.LocalDate.now();
                for (RequestDetailDTO detailDTO : req.getDetails()) {
                    if (detailDTO.getSpecificDate() != null) {
                        if (detailDTO.getSpecificDate().isBefore(today)) {
                            throw new BadRequestException(
                                "Ngày " + detailDTO.getSpecificDate() + " đã qua. Chỉ được chọn ngày hôm nay hoặc tương lai.");
                        }
                    }
                }
            }
        }

        Request r = new Request();
        r.setEmployee(emp);
        r.setTitle(req.getTitle().trim());
        r.setType(type);
        r.setDescription(req.getDescription());
        r.setFileUrl(req.getFileUrl());
        r.setFileName(req.getFileName());

        if (req.getDetails() != null) {
            for (RequestDetailDTO detailDTO : req.getDetails()) {
                RequestDetail detail = new RequestDetail();
                detail.setRequest(r);
                detail.setSpecificDate(detailDTO.getSpecificDate());
                detail.setLeaveSession(detailDTO.getLeaveSession());
                detail.setOvertimeHours(detailDTO.getOvertimeHours());
                if (detailDTO.getCheckIn() != null && !detailDTO.getCheckIn().isBlank()) {
                    detail.setCheckIn(java.time.LocalTime.parse(detailDTO.getCheckIn()));
                }
                if (detailDTO.getCheckOut() != null && !detailDTO.getCheckOut().isBlank()) {
                    detail.setCheckOut(java.time.LocalTime.parse(detailDTO.getCheckOut()));
                }
                r.getDetails().add(detail);
            }
        }

        Request savedRequest = requestRepo.save(r);

        // --- Bắn thông báo qua WebSockets ---
        try {
            User senderUser = userRepository.findByEmployeeId(employeeId).orElse(null);
            if (senderUser != null) {
                String role = senderUser.getRole().name();
                List<User> targets = new ArrayList<>();
                if ("EMPLOYEE".equals(role)) {
                    // TÌm Trưởng phòng
                    if (emp.getDepartment() != null && emp.getDepartment().getManager() != null) {
                        userRepository.findByEmployeeId(emp.getDepartment().getManager().getId())
                                .ifPresent(targets::add);
                    }
                    // Fallback nếu phòng không có Manager -> Gửi cho Admin
                    if (targets.isEmpty()) {
                        targets.addAll(userRepository.findByRole(Role.ADMIN));
                    }
                } else {
                    // Manager/Admin gửi đơn -> Bắn cho Admin
                    targets.addAll(userRepository.findByRole(Role.ADMIN));
                }

                // === ĐẶC BIỆT: ĐƠN THÔI VIỆC CHỈ GỬI CHO ADMIN ===
                if (savedRequest.getType() == RequestType.RESIGNATION) {
                    targets.clear();
                    targets.addAll(userRepository.findByRole(Role.ADMIN));
                }

                if (!targets.isEmpty()) {
                    notificationService.sendTargetedNotification(
                            senderUser,
                            targets,
                            "Đơn mới từ " + emp.getFullName(),
                            "Yêu cầu: " + savedRequest.getTitle()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("[NOTIFICATION ERROR - createRequest] " + e.getMessage());
            e.printStackTrace();
        }

        userRepository.findByEmployeeId(employeeId).ifPresent(u -> 
            logService.log(u, "CREATE", "Nhân viên " + emp.getFullName() + " đã gửi đơn: " + savedRequest.getTitle() + " (" + savedRequest.getType() + ")")
        );
        return RequestDTO.from(savedRequest);
    }

    @Transactional
    public RequestDTO updateRequest(Long requestId, Long employeeId, com.example.qlns.DTO.Request.CreateRequestRequest req) {
        Request r = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));

        if (!r.getEmployee().getId().equals(employeeId))
            throw new ForbiddenException("Bạn không có quyền sửa đơn này");

        if (r.getStatus() != RequestStatus.PENDING)
            throw new BadRequestException("Chỉ được sửa đơn khi chưa được duyệt");

        if (req.getTitle() != null && !req.getTitle().isBlank())
            r.setTitle(req.getTitle().trim());
        if (req.getType() != null)
            r.setType(req.getType());
        if (req.getDescription() != null)
            r.setDescription(req.getDescription());
        if (req.getFileUrl() != null)
            r.setFileUrl(req.getFileUrl());
        if (req.getFileName() != null)
            r.setFileName(req.getFileName());

        if (req.getDetails() != null) {
            r.getDetails().clear();
            for (RequestDetailDTO detailDTO : req.getDetails()) {
                RequestDetail detail = new RequestDetail();
                detail.setRequest(r);
                detail.setSpecificDate(detailDTO.getSpecificDate());
                detail.setLeaveSession(detailDTO.getLeaveSession());
                detail.setOvertimeHours(detailDTO.getOvertimeHours());
                if (detailDTO.getCheckIn() != null && !detailDTO.getCheckIn().isBlank()) {
                    detail.setCheckIn(java.time.LocalTime.parse(detailDTO.getCheckIn()));
                }
                if (detailDTO.getCheckOut() != null && !detailDTO.getCheckOut().isBlank()) {
                    detail.setCheckOut(java.time.LocalTime.parse(detailDTO.getCheckOut()));
                }
                r.getDetails().add(detail);
            }
        }

        return RequestDTO.from(requestRepo.save(r));
    }

    @Transactional
    public void cancelRequest(Long requestId, Long employeeId) {
        Request r = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));

        if (!r.getEmployee().getId().equals(employeeId))
            throw new ForbiddenException("Bạn không có quyền huỷ đơn này");

        if (r.getStatus() != RequestStatus.PENDING)
            throw new BadRequestException("Chỉ được huỷ đơn khi chưa được duyệt");

        r.setStatus(RequestStatus.CANCELLED);
        requestRepo.save(r);

        userRepository.findByEmployeeId(employeeId).ifPresent(u -> 
            logService.log(u, "UPDATE", "Nhân viên " + r.getEmployee().getFullName() + " đã hủy đơn: " + r.getTitle())
        );
    }

    // ── Scheduler: Tự động hủy đơn quá hạn ──────────
    @Scheduled(cron = "0 0 1 * * *") // Chạy lúc 1h sáng mỗi ngày
    @Transactional
    public void autoExpireRequests() {
        List<Request> expired = requestRepo.findExpiredPendingRequests(LocalDate.now());
        if (!expired.isEmpty()) {
            expired.forEach(r -> r.setStatus(RequestStatus.EXPIRED));
            requestRepo.saveAll(expired);
        }
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getByDepartment(Long deptId) {
        return requestRepo.findByDepartmentId(deptId)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getByDepartmentAndStatus(Long deptId, String status) {
        RequestStatus reqStatus = RequestStatus.valueOf(status);
        return requestRepo.findByDepartmentIdAndStatus(deptId, reqStatus)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getEmployeeRequestsByDepartmentAndStatus(Long deptId, String status, Integer month, Integer year, Long empId) {
        // Nếu là PENDING thì dùng query phân cấp (chỉ lấy EMPLOYEE)
        if ("PENDING".equalsIgnoreCase(status)) {
            return requestRepo.findEmployeePendingRequests(deptId, empId, month, year)
                    .stream().map(RequestDTO::from).collect(Collectors.toList());
        }
        // Các trạng thái khác (Lịch sử) thì lấy tất cả trong phòng ban
        return getFilteredRequests(status, month, year, deptId, empId);
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getManagerRequestsByStatus(String status, Integer month, Integer year, Long deptId, Long empId) {
        // Nếu là PENDING thì dùng query phân cấp (chỉ lấy MANAGER)
        if ("PENDING".equalsIgnoreCase(status)) {
            return requestRepo.findManagerPendingRequests(deptId, empId, month, year)
                    .stream().map(RequestDTO::from).collect(Collectors.toList());
        }
        // Các trạng thái khác (Lịch sử) thì Admin xem tất cả
        return getFilteredRequests(status, month, year, deptId, empId);
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getPendingByDepartment(Long deptId) {
        return requestRepo.findByDepartmentIdAndStatus(deptId, RequestStatus.PENDING)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RequestDTO getRequestById(Long id) {
        Request r = requestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn id=" + id));
        return RequestDTO.from(r);
    }

    @Transactional
    public RequestDTO reviewRequest(Long requestId, Long reviewerId, ReviewRequestRequest req) {
        Request r = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));

        if (r.getStatus() != RequestStatus.PENDING)
            throw new BadRequestException("Đơn này đã được xử lý rồi");

        Employee reviewer = employeeRepo.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người duyệt"));

        if (req.isApproved()) {
            r.setStatus(RequestStatus.APPROVED);
            // Cập nhật quỹ phép (chỉ loại LEAVE_ANNUAL)
            if (r.getType() == RequestType.LEAVE_ANNUAL) {
                Employee employee = r.getEmployee();
                double duration = calculateLeaveDurationForEntity(r.getDetails());
                double used = employee.getLeaveDaysUsed() != null ? employee.getLeaveDaysUsed() : 0.0;
                employee.setLeaveDaysUsed(used + duration);
                employeeRepo.save(employee);
            }
            // Bổ sung công
            else if (r.getType() == RequestType.PUNCH_CORRECTION) {
                for (RequestDetail detail : r.getDetails()) {
                    LocalDate date = detail.getSpecificDate();
                    Attendance att = attendanceRepo.findByEmployeeIdAndDate(r.getEmployee().getId(), date)
                            .orElse(new Attendance());
                    
                    if (att.getId() == null) {
                        att.setEmployee(r.getEmployee());
                        att.setDate(date);
                    }
                    
                    if (detail.getCheckIn() != null) att.setCheckIn(java.time.LocalDateTime.of(date, detail.getCheckIn()));
                    if (detail.getCheckOut() != null) att.setCheckOut(java.time.LocalDateTime.of(date, detail.getCheckOut()));
                    
                    CompanySettings settings = settingsRepo.findById(1L).orElse(null);
                    
                    // Tính lại đi muộn dựa theo cấu hình công ty thay vì fix cứng 8:00
                    if (att.getCheckIn() != null) {
                        LocalTime standardStart = (settings != null && settings.getWorkStartTime() != null) 
                                                ? LocalTime.parse(settings.getWorkStartTime()) 
                                                : LocalTime.of(8, 0); // Fallback an toàn
                        
                        if (att.getCheckIn().toLocalTime().isAfter(standardStart)) {
                            long mins = java.time.Duration.between(standardStart, att.getCheckIn().toLocalTime()).toMinutes();
                            att.setLateMinutes((int) mins);
                        } else {
                            att.setLateMinutes(0);
                        }
                    }

                    // Recalculate work hours including lunch deduction
                    if (att.getCheckIn() != null && att.getCheckOut() != null) {
                        if (settings != null) {
                            double netHours = attendanceService.calculateNetWorkHours(att.getCheckIn(), att.getCheckOut(), settings);
                            att.setWorkHours((float) netHours);
                        }
                        
                        // Cập nhật trạng thái chấm công dựa trên giờ làm việc
                        if (att.getLateMinutes() > 0) {
                            att.setStatus(AttendanceStatus.LATE);
                        } else {
                            att.setStatus(AttendanceStatus.ON_TIME);
                        }
                    }

                    attendanceRepo.save(att);
                }
            }
            // Logic cho đơn xin THÔI VIỆC
            else if (r.getType() == RequestType.RESIGNATION) {
                employeeService.resign(r.getEmployee().getId());
            }
        } else {
            if (req.getRejectionReason() == null || req.getRejectionReason().isBlank())
                throw new BadRequestException("Phải nhập lý do từ chối");
            r.setStatus(RequestStatus.REJECTED);
            r.setRejectionReason(req.getRejectionReason());
        }

        r.setReviewedBy(reviewer);
        Request savedRequest = requestRepo.save(r);

        // --- Báo kết quả thông qua WebSockets ---
        try {
            User reviewerUser = userRepository.findByEmployeeId(reviewerId).orElse(null);
            User creatorUser = userRepository.findByEmployeeId(r.getEmployee().getId()).orElse(null);
            if (reviewerUser != null && creatorUser != null) {
                List<User> targets = new ArrayList<>();
                targets.add(creatorUser);
                String statusStr = req.isApproved() ? "đã được DUYỆT" : "BỊ TỪ CHỐI";
                notificationService.sendTargetedNotification(
                        reviewerUser,
                        targets,
                        "Kết quả đơn: " + r.getTitle(),
                        "Đơn của bạn " + statusStr + " bởi " + reviewer.getFullName()
                );
                
                // Log activity
                String logAction = req.isApproved() ? "APPROVE" : "REJECT";
                String logDesc = "Quản lý " + reviewer.getFullName() + " " + (req.isApproved() ? "đã duyệt" : "đã từ chối") + 
                                " đơn " + r.getTitle() + " của " + r.getEmployee().getFullName();
                logService.log(reviewerUser, logAction, logDesc);
            }
        } catch (Exception e) {
            System.err.println("[NOTIFICATION ERROR - reviewRequest] " + e.getMessage());
            e.printStackTrace();
        }

        return RequestDTO.from(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getAllRequests() {
        return requestRepo.findAllByOrderByCreatedAtDesc()
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getFilteredRequests(String status, Integer month, Integer year, Long deptId, Long empId) {
        RequestStatus reqStatus = (status == null || "ALL".equalsIgnoreCase(status)) ? null : RequestStatus.valueOf(status);
        return requestRepo.findFilteredRequests(reqStatus, month, year, deptId, empId)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDTO> getAllRequestsByStatus(String status) {
        if (status == null || "ALL".equalsIgnoreCase(status)) {
            return requestRepo.findAllByOrderByCreatedAtDesc()
                    .stream().map(RequestDTO::from).collect(Collectors.toList());
        }
        RequestStatus reqStatus = RequestStatus.valueOf(status);
        return requestRepo.findByStatusOrderByCreatedAtDesc(reqStatus)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    private double calculateLeaveDuration(List<RequestDetailDTO> details) {
        if (details == null) return 0.0;
        double total = 0.0;
        for (RequestDetailDTO d : details) {
            if (d.getLeaveSession() == LeaveSession.ALL_DAY) total += 1.0;
            else if (d.getLeaveSession() == LeaveSession.MORNING || d.getLeaveSession() == LeaveSession.AFTERNOON) total += 0.5;
        }
        return total;
    }

    private double calculateLeaveDurationForEntity(List<RequestDetail> details) {
        if (details == null) return 0.0;
        double total = 0.0;
        for (RequestDetail d : details) {
            if (d.getLeaveSession() == LeaveSession.ALL_DAY) total += 1.0;
            else if (d.getLeaveSession() == LeaveSession.MORNING || d.getLeaveSession() == LeaveSession.AFTERNOON) total += 0.5;
        }
        return total;
    }
}