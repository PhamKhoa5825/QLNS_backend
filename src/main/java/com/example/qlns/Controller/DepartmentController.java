package com.example.qlns.Controller;

import com.example.qlns.DTO.Response.*;
import com.example.qlns.Entity.*;
import com.example.qlns.Service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV1 - DepartmentController
// =============================================
@RestController
@RequestMapping("/api/departments")
class DepartmentController {
    private final DepartmentService deptService;
    private final EmployeeService empService;

    DepartmentController(DepartmentService deptService, EmployeeService empService) {
        this.deptService = deptService; this.empService = empService;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAll() {
        return ResponseEntity.ok(deptService.getAll().stream()
                .map(d -> DepartmentDTO.from(d, deptService.countEmployees(d.getId())))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getById(@PathVariable Long id) {
        Department d = deptService.getById(id);
        return ResponseEntity.ok(DepartmentDTO.from(d, deptService.countEmployees(id)));
    }

    @GetMapping("/{id}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployees(@PathVariable Long id) {
        return ResponseEntity.ok(empService.getByDepartment(id).stream()
                .map(e -> EmployeeDTO.from(e, empService.getRoleByEmployeeId(e.getId())))
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DepartmentDTO> create(@RequestBody Department dept) {
        Department saved = deptService.create(dept);
        return ResponseEntity.ok(DepartmentDTO.from(saved, 0));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> update(@PathVariable Long id, @RequestBody Department req) {
        Department updated = deptService.update(id, req);
        return ResponseEntity.ok(DepartmentDTO.from(updated, deptService.countEmployees(id)));
    }

    @PutMapping("/{deptId}/manager/{empId}")
    public ResponseEntity<DepartmentDTO> setManager(@PathVariable Long deptId, @PathVariable Long empId) {
        Department updated = deptService.setManager(deptId, empId);
        return ResponseEntity.ok(DepartmentDTO.from(updated, deptService.countEmployees(deptId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

