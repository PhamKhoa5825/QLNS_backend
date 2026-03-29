package com.example.qlns.Controller;

import com.example.qlns.DTO.Response.SalaryRecordDTO;
import com.example.qlns.Service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // ── ADMIN: Generate for entire company ────────────────────────────────────
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalaryRecordDTO>> generateAll(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.generateForAll(month, year));
    }

    // ── MANAGER / ADMIN: Generate for a department ────────────────────────────
    @PostMapping("/generate/department/{deptId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<SalaryRecordDTO>> generateForDepartment(
            @PathVariable Long deptId,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.generateForDepartment(deptId, month, year));
    }

    // ── EMPLOYEE: View own salary (official OR real-time estimate if not generated) ──
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<SalaryRecordDTO> getMyPayroll(
            @RequestParam Long empId,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getOrEstimate(empId, month, year));
    }

    // ── EMPLOYEE: Pure real-time estimate (never saved) ───────────────────────
    @GetMapping("/me/estimate")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<SalaryRecordDTO> getMyEstimate(
            @RequestParam Long empId,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.estimateForEmployee(empId, month, year));
    }

    // ── EMPLOYEE: View salary history ─────────────────────────────────────────
    @GetMapping("/me/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<SalaryRecordDTO>> getMyHistory(@RequestParam Long empId) {
        return ResponseEntity.ok(payrollService.getHistoryForEmployee(empId));
    }

    // ── MANAGER / ADMIN: View one employee salary ─────────────────────────────
    @GetMapping("/employee/{empId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<SalaryRecordDTO> getEmployeePayroll(
            @PathVariable Long empId,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getForEmployee(empId, month, year));
    }

    // ── MANAGER / ADMIN: View department salary list ─────────────────────────
    @GetMapping("/department/{deptId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<SalaryRecordDTO>> getDepartmentPayroll(
            @PathVariable Long deptId,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getForDepartment(deptId, month, year));
    }

    // ── ADMIN: View all salary records ────────────────────────────────────────
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalaryRecordDTO>> getAllPayroll(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getAll(month, year));
    }

    @GetMapping("/all/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalaryRecordDTO>> getAllSummary(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getSummaryForAll(month, year));
    }

    // ── ADMIN: Finalize a salary record ──────────────────────────────────────
    @PutMapping("/{id}/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalaryRecordDTO> finalize(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.finalize(id));
    }

    // ── ADMIN / MANAGER: Add note to a record ────────────────────────────────
    @PutMapping("/{id}/note")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<SalaryRecordDTO> addNote(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(payrollService.addNote(id, body.get("note")));
    }

    // ── ADMIN: Update employee base salary ────────────────────────────────────
    @PutMapping("/employees/{empId}/base-salary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateBaseSalary(
            @PathVariable Long empId, @RequestBody Map<String, Double> body) {
        payrollService.updateBaseSalary(empId, body.get("baseSalary"));
        return ResponseEntity.ok().build();
    }
}
