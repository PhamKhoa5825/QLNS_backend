package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateEmployeeRequest;
import com.example.qlns.DTO.Request.UpdateEmployeeRequest;
import com.example.qlns.DTO.Response.EmployeeDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.Gender;
import com.example.qlns.Enum.Role;
import com.example.qlns.Security.SecurityService;
import com.example.qlns.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV1 - EmployeeController
// =============================================
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService empService;
    private final SecurityService securityService;

    EmployeeController(EmployeeService empService, SecurityService securityService) {
        this.empService = empService;
        this.securityService = securityService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAll() {
        // Admin xem tất cả, Manager chỉ xem phòng ban mình
        if (securityService.isAdmin()) {
            return ResponseEntity.ok(empService.getAll());
        }
        Long deptId = securityService.getManagerDepartmentId();
        return ResponseEntity.ok(empService.getByDepartment(deptId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable Long id) {
        Employee emp = empService.getById(id);
        return ResponseEntity.ok(EmployeeDTO.from(emp, empService.getRoleByEmployeeId(id)));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(@PathVariable Long deptId) {
        // Manager chỉ xem được NV phòng ban mình, Admin xem tất cả
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(empService.getByDepartment(deptId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(empService.search(keyword).stream()
                .map(EmployeeDTO::from)
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

    @PutMapping("/{id}/resign")
    public ResponseEntity<Void> resign(@PathVariable Long id) {
        empService.resign(id);
        return ResponseEntity.noContent().build();
    }
}
