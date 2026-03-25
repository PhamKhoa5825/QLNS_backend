package com.example.qlns.Service;

import com.example.qlns.DTO.Request.CreateRequestRequest;
import com.example.qlns.DTO.Request.ReviewRequestRequest;
import com.example.qlns.DTO.Response.RequestDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.Request;
import com.example.qlns.Enum.RequestStatus;
import com.example.qlns.Enum.TargetRole;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    @Autowired private RequestRepository requestRepo;
    @Autowired private EmployeeRepository employeeRepo;

    // ── Nhân viên: xem đơn của mình ──────────────────────────
    @Transactional(readOnly = true)
    public List<RequestDTO> getMyRequests(Long employeeId) {
        return requestRepo.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    // ── Nhân viên: tạo đơn mới ───────────────────────────────
    @Transactional
    public RequestDTO createRequest(Long employeeId, CreateRequestRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank())
            throw new BadRequestException("Tiêu đề đơn không được để trống");

        Employee emp = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên"));

        Request r = new Request();
        r.setEmployee(emp);
        r.setTitle(req.getTitle().trim());
        r.setDescription(req.getDescription());
        r.setFileUrl(req.getFileUrl());
        r.setFileName(req.getFileName());

        // Thay đoạn set targetRole cũ bằng:
        if (req.getTargetRole() != null) {
            try {
                r.setTargetRole(TargetRole.valueOf(req.getTargetRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Nếu giá trị không hợp lệ, giữ mặc định MANAGER
            }
        }
        // Mặc định = "MANAGER" (đã set trong entity)

        return RequestDTO.from(requestRepo.save(r));
    }

    // ── Nhân viên: sửa đơn khi còn PENDING ──────────────────
    @Transactional
    public RequestDTO updateRequest(Long requestId, Long employeeId, CreateRequestRequest req) {
        Request r = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));

        if (!r.getEmployee().getId().equals(employeeId))
            throw new ForbiddenException("Bạn không có quyền sửa đơn này");

        if (r.getStatus() != RequestStatus.PENDING)
            throw new BadRequestException("Chỉ được sửa đơn khi chưa được duyệt");

        if (req.getTitle() != null && !req.getTitle().isBlank())
            r.setTitle(req.getTitle().trim());
        if (req.getDescription() != null)
            r.setDescription(req.getDescription());
        if (req.getFileUrl() != null)
            r.setFileUrl(req.getFileUrl());
        if (req.getFileName() != null)
            r.setFileName(req.getFileName());

        return RequestDTO.from(requestRepo.save(r));
    }

    // ── Nhân viên: huỷ đơn khi còn PENDING ──────────────────
    @Transactional
    public void cancelRequest(Long requestId, Long employeeId) {
        Request r = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));

        if (!r.getEmployee().getId().equals(employeeId))
            throw new ForbiddenException("Bạn không có quyền huỷ đơn này");

        if (r.getStatus() != RequestStatus.PENDING)
            throw new BadRequestException("Chỉ được huỷ đơn khi chưa được duyệt");

        requestRepo.delete(r);
    }

    // ── Manager: xem tất cả đơn phòng ban ────────────────────
    @Transactional(readOnly = true)
    public List<RequestDTO> getByDepartment(Long deptId) {
        return requestRepo.findByDepartmentIdAndTargetRole(deptId)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    // ── Manager: xem đơn chờ duyệt phòng ban ─────────────────
    @Transactional(readOnly = true)
    public List<RequestDTO> getPendingByDepartment(Long deptId) {
        return requestRepo.findByDepartmentIdAndStatusAndTargetRoleManager(deptId, RequestStatus.PENDING)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }

    // ── Manager/Admin: duyệt hoặc từ chối ────────────────────
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
        } else {
            if (req.getRejectionReason() == null || req.getRejectionReason().isBlank())
                throw new BadRequestException("Phải nhập lý do từ chối");
            r.setStatus(RequestStatus.REJECTED);
            r.setRejectionReason(req.getRejectionReason());
        }

        r.setReviewedBy(reviewer);
        return RequestDTO.from(requestRepo.save(r));
    }

    // ── Admin: xem tất cả đơn gửi tới Admin ───────────────────
    @Transactional(readOnly = true)
    public List<RequestDTO> getAllRequests() {
        return requestRepo.findByTargetRoleOrderByCreatedAtDesc(TargetRole.ADMIN)
                .stream().map(RequestDTO::from).collect(Collectors.toList());
    }
}