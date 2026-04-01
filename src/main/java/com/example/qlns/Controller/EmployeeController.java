package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateEmployeeRequest;
import com.example.qlns.DTO.Request.UpdateEmployeeRequest;
import com.example.qlns.DTO.Response.EmployeeDetailDTO;
import com.example.qlns.DTO.Response.EmployeeDTO;
import com.example.qlns.DTO.Response.EmployeeSummaryDTO;
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
    public ResponseEntity<EmployeeDTO> getById(@PathVariable("id") Long id) {
        Employee emp = empService.getById(id);
        return ResponseEntity.ok(EmployeeDTO.from(emp, empService.getRoleByEmployeeId(id)));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(@PathVariable("deptId") Long deptId) {
        // Manager chỉ xem được NV phòng ban mình, Admin xem tất cả
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(empService.getByDepartment(deptId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> search(@RequestParam("keyword") String keyword) {
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
        if (req.getBaseSalary() != null) emp.setBaseSalary(req.getBaseSalary());

        Role role = Role.EMPLOYEE; 
        if (req.getRole() != null && securityService.isAdmin()) {
            try {
                role = Role.valueOf(req.getRole());
            } catch (IllegalArgumentException e) {
                // Default to EMPLOYEE if role is invalid
            }
        }
        
        if (req.getAvatarUrl() != null) emp.setAvatarUrl(req.getAvatarUrl());
        
        Employee saved = empService.create(emp, req.getEmail(), req.getPassword(), role);
        return ResponseEntity.ok(EmployeeDTO.from(saved, role.name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable("id") Long id, @RequestBody UpdateEmployeeRequest req) {
        Employee emp = new Employee();
        emp.setFullName(req.getFullName());
        emp.setPhone(req.getPhone());
        emp.setAddress(req.getAddress());
        emp.setPosition(req.getPosition());
        emp.setAvatarUrl(req.getAvatarUrl());
        if (req.getBaseSalary() != null) emp.setBaseSalary(req.getBaseSalary());
        
        Employee updated = empService.update(id, emp);
        return ResponseEntity.ok(EmployeeDTO.from(updated, empService.getRoleByEmployeeId(id)));
    }

    @PutMapping("/{id}/resign")
    public ResponseEntity<Void> resign(@PathVariable("id") Long id) {
        empService.resign(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable("id") Long id) {
        empService.reactivate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * API Rút gọn cho trang chủ (Employee App)
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<EmployeeSummaryDTO> getSummary(@PathVariable("id") Long id) {
        return ResponseEntity.ok(empService.getEmployeeSummary(id));
    }

    /**
     * API Chi tiết cho trang cá nhân (Employee App)
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<EmployeeDetailDTO> getDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(empService.getEmployeeDetail(id));
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<Double> getBaseSalary(@PathVariable("id") Long id) {
        return ResponseEntity.ok(empService.getById(id).getBaseSalary());
    }

    @PutMapping("/{id}/salary")
    public ResponseEntity<Void> updateBaseSalary(@PathVariable("id") Long id, @RequestBody java.util.Map<String, Double> payload) {
        Employee emp = empService.getById(id);
        emp.setBaseSalary(payload.get("baseSalary"));
        empService.update(id, emp);
        return ResponseEntity.ok().build();
    }
}
