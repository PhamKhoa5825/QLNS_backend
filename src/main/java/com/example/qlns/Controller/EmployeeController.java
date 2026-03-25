package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateEmployeeRequest;
import com.example.qlns.DTO.Request.UpdateEmployeeRequest;
import com.example.qlns.DTO.Response.EmployeeDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.Gender;
import com.example.qlns.Enum.Role;
import com.example.qlns.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * EmployeeController v2 — Thêm endpoint resign-check + resign cascade
 *
 * Endpoint mới:
 * - GET  /api/employees/{id}/resign-check  → kiểm tra ảnh hưởng trước nghỉ việc
 * - PUT  /api/employees/{id}/resign        → nghỉ việc có cascade (cải thiện)
 */
@RestController
@RequestMapping("/api/employees")
class EmployeeController {
    private final EmployeeService empService;

    EmployeeController(EmployeeService empService) {
        this.empService = empService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAll() {
        return ResponseEntity.ok(empService.getAllDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(empService.getByIdDTO(id));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(empService.getByDepartmentDTO(deptId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(empService.searchDTO(keyword));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody CreateEmployeeRequest req) {
        Employee emp = new Employee();
        emp.setFullName(req.getFullName());
        emp.setPhone(req.getPhone());
        emp.setAddress(req.getAddress());
        emp.setPosition(req.getPosition());
        if (req.getJoinDate() != null) emp.setJoinDate(LocalDate.parse(req.getJoinDate()));
        if (req.getDateOfBirth() != null) emp.setDateOfBirth(LocalDate.parse(req.getDateOfBirth()));
        if (req.getGender() != null) emp.setGender(Gender.valueOf(req.getGender()));

        Role role = Role.EMPLOYEE;
        Employee saved = empService.create(emp, req.getEmail(), req.getPassword(), role);
        return ResponseEntity.ok(EmployeeDTO.from(saved, role.name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @RequestBody UpdateEmployeeRequest req) {
        Employee emp = new Employee();
        emp.setFullName(req.getFullName());
        emp.setPhone(req.getPhone());
        emp.setAddress(req.getAddress());
        emp.setPosition(req.getPosition());
        emp.setAvatarUrl(req.getAvatarUrl());
        Employee updated = empService.update(id, emp);
        return ResponseEntity.ok(EmployeeDTO.from(updated, empService.getRoleByEmployeeId(id)));
    }

    /**
     * MỚI: Kiểm tra ảnh hưởng trước khi cho nghỉ việc
     * FE gọi endpoint này trước, hiện dialog cảnh báo, rồi mới gọi resign.
     */
    @GetMapping("/{id}/resign-check")
    public ResponseEntity<Map<String, Object>> checkResignImpact(@PathVariable Long id) {
        return ResponseEntity.ok(empService.checkResignImpact(id));
    }

    /**
     * CẢI THIỆN: Nghỉ việc có cascade
     * Trả về JSON thay vì void, cho FE biết đã xử lý gì.
     */
    @PutMapping("/{id}/resign")
    public ResponseEntity<Map<String, Object>> resign(@PathVariable Long id) {
        return ResponseEntity.ok(empService.resignWithCascade(id));
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        empService.reactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<EmployeeDTO> updateEmployeeRole(
            @PathVariable Long id,
            @RequestBody com.example.qlns.DTO.Request.UpdateRoleRequest req) {
        return ResponseEntity.ok(empService.updateRole(id, req.getRole()));
    }
}
