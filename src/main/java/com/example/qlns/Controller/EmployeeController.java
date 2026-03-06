package com.example.qlns.Controller;

import com.example.qlns.DTO.CreateEmployeeRequest;
import com.example.qlns.DTO.EmployeeDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// =============================================
// EMPLOYEE CONTROLLER - dùng DTO
// =============================================
@RestController
@RequestMapping("/api/employees")
class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAll() {
        List<EmployeeDTO> result = employeeService.getAll()
                .stream()
                .map(EmployeeDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(EmployeeDTO.from(employeeService.getById(id)));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<EmployeeDTO>> getByDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> result = employeeService.getByDepartment(departmentId)
                .stream()
                .map(EmployeeDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> search(@RequestParam String keyword) {
        List<EmployeeDTO> result = employeeService.search(keyword)
                .stream()
                .map(EmployeeDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> create(@RequestBody CreateEmployeeRequest request) {
        Employee saved = employeeService.create(
                request.toEmployee(),
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(EmployeeDTO.from(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id,
                                              @RequestBody Employee employee) {
        return ResponseEntity.ok(EmployeeDTO.from(employeeService.update(id, employee)));
    }

    @PutMapping("/{id}/terminate")
    public ResponseEntity<Void> terminate(@PathVariable Long id) {
        employeeService.terminate(id);
        return ResponseEntity.noContent().build();
    }
}
