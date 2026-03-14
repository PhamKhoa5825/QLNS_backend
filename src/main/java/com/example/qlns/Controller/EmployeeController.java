package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateEmployeeRequest;
import com.example.qlns.DTO.Request.UpdateEmployeeRequest;
import com.example.qlns.DTO.Response.EmployeeDetailDTO;
import com.example.qlns.DTO.Response.EmployeeDTO;
import com.example.qlns.DTO.Response.EmployeeSummaryDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.Gender;
import com.example.qlns.Enum.Role;
import com.example.qlns.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV1 - Bộ điều khiển Nhân viên
// =============================================
@RestController
@RequestMapping("/api/employees")
class EmployeeController {
    private final EmployeeService empService;

    EmployeeController(EmployeeService empService) {
        this.empService = empService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAll() {
        return ResponseEntity.ok(empService.getAll().stream()
                .map(e -> EmployeeDTO.from(e, empService.getRoleByEmployeeId(e.getId())))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable Long id) {
        Employee emp = empService.getById(id);
        return ResponseEntity.ok(EmployeeDTO.from(emp, empService.getRoleByEmployeeId(id)));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(empService.getByDepartment(deptId).stream()
                .map(e -> EmployeeDTO.from(e, empService.getRoleByEmployeeId(e.getId())))
                .collect(Collectors.toList()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(empService.search(keyword).stream()
                .map(e -> EmployeeDTO.from(e, empService.getRoleByEmployeeId(e.getId())))
                .collect(Collectors.toList()));
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
    public ResponseEntity<EmployeeDetailDTO> update(@PathVariable Long id, @RequestBody UpdateEmployeeRequest req) {
        Employee emp = new Employee();
        emp.setEmail(req.getEmail());
        emp.setPhone(req.getPhone());
        emp.setAddress(req.getAddress());
        emp.setPosition(req.getPosition());
        emp.setAvatarUrl(req.getAvatarUrl());
        
        empService.update(id, emp);
        return ResponseEntity.ok(empService.getEmployeeDetail(id));
    }

    @PutMapping("/{id}/resign")
    public ResponseEntity<Void> resign(@PathVariable Long id) {
        empService.resign(id);
        return ResponseEntity.noContent().build();
    }

    // API Rút gọn cho trang chủ (Họ tên, Ảnh)
    @GetMapping("/{id}/summary")
    public ResponseEntity<EmployeeSummaryDTO> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(empService.getEmployeeSummary(id));
    }

    // API Chi tiết cho trang cá nhân (Đầy đủ thông tin)
    @GetMapping("/{id}/detail")
    public ResponseEntity<EmployeeDetailDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(empService.getEmployeeDetail(id));
    }
}
