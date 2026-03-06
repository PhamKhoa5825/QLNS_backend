package com.example.qlns.Controller;

import com.example.qlns.DTO.*;
import com.example.qlns.Entity.*;
import com.example.qlns.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// =============================================
// DEPARTMENT CONTROLLER - dùng DTO
// =============================================
@RestController
@RequestMapping("/api/departments")
class DepartmentController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    public DepartmentController(DepartmentService departmentService,
                                EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAll() {
        List<DepartmentDTO> result = departmentService.getAll()
                .stream()
                .map(dept -> {
                    int count = employeeService.getByDepartment(dept.getId()).size();
                    return DepartmentDTO.from(dept, count);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getById(@PathVariable Long id) {
        Department dept = departmentService.getById(id);
        int count = employeeService.getByDepartment(id).size();
        return ResponseEntity.ok(DepartmentDTO.from(dept, count));
    }

    @GetMapping("/{id}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployees(@PathVariable Long id) {
        List<EmployeeDTO> result = employeeService.getByDepartment(id)
                .stream()
                .map(EmployeeDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> create(@RequestBody Department department) {
        Department saved = departmentService.create(department);
        return ResponseEntity.ok(DepartmentDTO.from(saved, 0));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> update(@PathVariable Long id,
                                                @RequestBody Department department) {
        Department updated = departmentService.update(id, department);
        int count = employeeService.getByDepartment(id).size();
        return ResponseEntity.ok(DepartmentDTO.from(updated, count));
    }

    @PutMapping("/{departmentId}/manager/{employeeId}")
    public ResponseEntity<DepartmentDTO> setManager(@PathVariable Long departmentId,
                                                    @PathVariable Long employeeId) {
        Department updated = departmentService.setManager(departmentId, employeeId);
        int count = employeeService.getByDepartment(departmentId).size();
        return ResponseEntity.ok(DepartmentDTO.from(updated, count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

