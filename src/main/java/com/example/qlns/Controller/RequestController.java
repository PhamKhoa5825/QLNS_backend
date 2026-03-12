package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.*;
import com.example.qlns.DTO.Response.*;
import com.example.qlns.Entity.User;
import com.example.qlns.Repository.UserRepository;
import com.example.qlns.Security.SecurityService;
import com.example.qlns.Service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired private RequestService requestService;
    @Autowired private SecurityService securityService;
    @Autowired private UserRepository userRepo;

    // ── Nhân viên: xem đơn của mình ──────────────────────────
    // GET /api/requests/employee/{empId}
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<RequestDTO>> getMyRequests(
            @PathVariable Long empId) {
        return ResponseEntity.ok(requestService.getMyRequests(empId));
    }

    // ── Nhân viên: tạo đơn ───────────────────────────────────
    // POST /api/requests/employee/{empId}
    @PostMapping("/employee/{empId}")
    public ResponseEntity<RequestDTO> createRequest(
            @PathVariable Long empId,
            @RequestBody CreateRequestRequest req) {
        return ResponseEntity.ok(requestService.createRequest(empId, req));
    }

    // ── Nhân viên: sửa đơn ───────────────────────────────────
    // PUT /api/requests/{id}/employee/{empId}
    @PutMapping("/{id}/employee/{empId}")
    public ResponseEntity<RequestDTO> updateRequest(
            @PathVariable Long id,
            @PathVariable Long empId,
            @RequestBody CreateRequestRequest req) {
        return ResponseEntity.ok(requestService.updateRequest(id, empId, req));
    }

    // ── Nhân viên: huỷ đơn ───────────────────────────────────
    // DELETE /api/requests/{id}/employee/{empId}
    @DeleteMapping("/{id}/employee/{empId}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long id,
            @PathVariable Long empId) {
        requestService.cancelRequest(id, empId);
        return ResponseEntity.noContent().build();
    }

    // ── Manager: xem tất cả đơn phòng ban ────────────────────
    // GET /api/requests/department/{deptId}
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<RequestDTO>> getByDepartment(
            @PathVariable Long deptId) {
        // Manager chỉ xem được đơn phòng ban mình, Admin xem tất cả
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(requestService.getByDepartment(deptId));
    }

    // ── Manager: lọc đơn phòng ban theo trạng thái ──────────
    // GET /api/requests/department/{deptId}/status?status=APPROVED
    @GetMapping("/department/{deptId}/status")
    public ResponseEntity<List<RequestDTO>> getByDepartmentAndStatus(
            @PathVariable Long deptId,
            @RequestParam String status) {
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(requestService.getByDepartmentAndStatus(deptId, status));
    }

    // ── Manager: đơn chờ duyệt phòng ban ─────────────────────
    // GET /api/requests/department/{deptId}/pending
    @GetMapping("/department/{deptId}/pending")
    public ResponseEntity<List<RequestDTO>> getPendingByDepartment(
            @PathVariable Long deptId) {
        // Manager chỉ xem được đơn chờ duyệt phòng ban mình
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(requestService.getPendingByDepartment(deptId));
    }

    // ── Xem chi tiết đơn ──────────────────────────────────────
    // GET /api/requests/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    // ── Manager/Admin: duyệt hoặc từ chối ────────────────────
    // PUT /api/requests/{id}/review/employee/{reviewerId}
    @PutMapping("/{id}/review/employee/{reviewerId}")
    public ResponseEntity<RequestDTO> reviewRequest(
            @PathVariable Long id,
            @PathVariable Long reviewerId,
            @RequestBody ReviewRequestRequest req) {
        return ResponseEntity.ok(requestService.reviewRequest(id, reviewerId, req));
    }

    // ── Thêm: Endpoint để App gọi đơn giản (Dùng trong RequestActivity.java) ──
    @PutMapping("/{id}/status")
    public ResponseEntity<RequestDTO> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        // Tạm thời lấy reviewerId là manager của dept hoặc hardcode reviewerId nếu không có context chi tiết cho endpoint này
        // Ở đây ta có thể lấy reviewerId từ SecurityContext
        Long reviewerUserId = securityService.getCurrentUserId();
        User user = userRepo.findById(reviewerUserId).orElseThrow();
        Long reviewerId = user.getEmployeeId();

        ReviewRequestRequest reviewRequest = new ReviewRequestRequest();
        reviewRequest.setApproved("APPROVED".equalsIgnoreCase(status));
        if (!reviewRequest.isApproved()) {
            reviewRequest.setRejectionReason("Từ chối bởi quản lý");
        }

        return ResponseEntity.ok(requestService.reviewRequest(id, reviewerId, reviewRequest));
    }

    // ── Admin: xem tất cả đơn ────────────────────────────────
    // GET /api/requests
    @GetMapping
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    // ── Admin: lọc tất cả đơn theo trạng thái ──────────────
    // GET /api/requests/status?status=PENDING
    @GetMapping("/status")
    public ResponseEntity<List<RequestDTO>> getAllRequestsByStatus(@RequestParam String status) {
        return ResponseEntity.ok(requestService.getAllRequestsByStatus(status));
    }
}