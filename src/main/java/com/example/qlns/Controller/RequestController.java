package com.example.qlns.Controller;

import com.example.qlns.DTO.Response.RequestDTO;
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
    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<RequestDTO>> getMyRequests(
            @PathVariable("empId") Long empId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
        return ResponseEntity.ok(requestService.getMyRequests(empId, month, year));
    }

    // ── Nhân viên: tạo đơn ───────────────────────────────────
    @PostMapping("/employee/{empId}")
    public ResponseEntity<RequestDTO> createRequest(
            @PathVariable("empId") Long empId,
            @RequestBody com.example.qlns.DTO.Request.CreateRequestRequest req) {
        return ResponseEntity.ok(requestService.createRequest(empId, req));
    }

    // ── Nhân viên: sửa đơn ───────────────────────────────────
    @PutMapping("/{id}/employee/{empId}")
    public ResponseEntity<RequestDTO> updateRequest(
            @PathVariable("id") Long id,
            @PathVariable("empId") Long empId,
            @RequestBody com.example.qlns.DTO.Request.CreateRequestRequest req) {
        return ResponseEntity.ok(requestService.updateRequest(id, empId, req));
    }

    // ── Nhân viên: huỷ đơn ───────────────────────────────────
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable("id") Long id,
            @RequestParam("empId") Long empId) {
        requestService.cancelRequest(id, empId);
        return ResponseEntity.noContent().build();
    }

    // ── Manager: xem tất cả đơn phòng ban ────────────────────
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<RequestDTO>> getByDepartment(
            @PathVariable("deptId") Long deptId) {
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(requestService.getByDepartment(deptId));
    }

    // ── Manager: lọc đơn phòng ban theo trạng thái ──────────
    @GetMapping("/department/{deptId}/status")
    public ResponseEntity<List<RequestDTO>> getByDepartmentAndStatus(
            @PathVariable("deptId") Long deptId,
            @RequestParam("status") String status,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "empId", required = false) Long empId) {
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(requestService.getEmployeeRequestsByDepartmentAndStatus(deptId, status, month, year, empId));
    }

    // ── Manager: đơn chờ duyệt phòng ban ─────────────────────
    @GetMapping("/department/{deptId}/pending")
    public ResponseEntity<List<RequestDTO>> getPendingByDepartment(
            @PathVariable("deptId") Long deptId) {
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(requestService.getPendingByDepartment(deptId));
    }

    // ── Xem chi tiết đơn ──────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<RequestDTO> getRequestById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    // ── Manager/Admin: duyệt hoặc từ chối ────────────────────
    @PutMapping("/{id}/review/employee/{reviewerId}")
    public ResponseEntity<RequestDTO> reviewRequest(
            @PathVariable("id") Long id,
            @PathVariable("reviewerId") Long reviewerId,
            @RequestBody com.example.qlns.DTO.Request.ReviewRequestRequest req) {
        return ResponseEntity.ok(requestService.reviewRequest(id, reviewerId, req));
    }

    // ── Endpoint để App gọi đơn giản (Dùng trong RequestActivity.java) ──
    @PutMapping("/{id}/status")
    public ResponseEntity<RequestDTO> updateRequestStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {
        Long reviewerUserId = securityService.getCurrentUserId();
        User user = userRepo.findById(reviewerUserId).orElseThrow();
        Long reviewerId = user.getEmployeeId();

        com.example.qlns.DTO.Request.ReviewRequestRequest reviewRequest = new com.example.qlns.DTO.Request.ReviewRequestRequest();
        reviewRequest.setApproved("APPROVED".equalsIgnoreCase(status));
        if (!reviewRequest.isApproved()) {
            reviewRequest.setRejectionReason("Từ chối bởi quản lý");
        }

        return ResponseEntity.ok(requestService.reviewRequest(id, reviewerId, reviewRequest));
    }

    // ── Admin: xem tất cả đơn ────────────────────────────────
    @GetMapping
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    // ── Admin: lọc tất cả đơn theo trạng thái ──────────────
    @GetMapping("/status")
    public ResponseEntity<List<RequestDTO>> getAllRequestsByStatus(
            @RequestParam("status") String status,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "empId", required = false) Long empId) {
        return ResponseEntity.ok(requestService.getManagerRequestsByStatus(status, month, year, deptId, empId));
    }
}