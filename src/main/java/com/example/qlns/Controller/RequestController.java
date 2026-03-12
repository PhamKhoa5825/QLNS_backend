package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.*;
import com.example.qlns.DTO.Response.*;
import com.example.qlns.Service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired private RequestService requestService;

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
        return ResponseEntity.ok(requestService.getByDepartment(deptId));
    }

    // ── Manager: đơn chờ duyệt phòng ban ─────────────────────
    // GET /api/requests/department/{deptId}/pending
    @GetMapping("/department/{deptId}/pending")
    public ResponseEntity<List<RequestDTO>> getPendingByDepartment(
            @PathVariable Long deptId) {
        return ResponseEntity.ok(requestService.getPendingByDepartment(deptId));
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

    // ── Admin: xem tất cả đơn ────────────────────────────────
    // GET /api/requests
    @GetMapping
    public ResponseEntity<List<RequestDTO>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }
}