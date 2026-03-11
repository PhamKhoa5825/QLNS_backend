package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.ApproveLeaveRequest;
import com.example.qlns.DTO.Request.CreateLeaveRequest;
import com.example.qlns.DTO.Response.LeaveBalanceDTO;
import com.example.qlns.DTO.Response.LeaveRequestDTO;
import com.example.qlns.Entity.LeaveBalance;
import com.example.qlns.Entity.LeaveRequest;
import com.example.qlns.Enum.LeaveType;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.LeaveBalanceRepository;
import com.example.qlns.Service.EmployeeService;
import com.example.qlns.Service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV3 - LeaveRequestController
// =============================================
@RestController
@RequestMapping("/api/leaves")
class LeaveRequestController {
    private final LeaveRequestService leaveService;
    private final EmployeeService empService;
    private final LeaveBalanceRepository balanceRepo;

    LeaveRequestController(LeaveRequestService leaveService, EmployeeService empService,
                           LeaveBalanceRepository balanceRepo) {
        this.leaveService = leaveService;
        this.empService = empService;
        this.balanceRepo = balanceRepo;
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<List<LeaveRequestDTO>> getByEmployee(@PathVariable Long empId) {
        return ResponseEntity.ok(leaveService.getByEmployee(empId).stream()
                .map(LeaveRequestDTO::from).collect(Collectors.toList()));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<LeaveRequestDTO>> getByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(leaveService.getByDepartment(deptId).stream()
                .map(LeaveRequestDTO::from).collect(Collectors.toList()));
    }

    @GetMapping("/department/{deptId}/pending")
    public ResponseEntity<List<LeaveRequestDTO>> getPending(@PathVariable Long deptId) {
        return ResponseEntity.ok(leaveService.getPendingByDepartment(deptId).stream()
                .map(LeaveRequestDTO::from).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDTO> create(@RequestBody CreateLeaveRequest req) {
        LeaveRequest lr = new LeaveRequest();
        lr.setEmployee(empService.getById(req.getEmployeeId()));
        lr.setLeaveType(LeaveType.valueOf(req.getLeaveType()));
        lr.setStartDate(LocalDate.parse(req.getStartDate()));
        lr.setEndDate(LocalDate.parse(req.getEndDate()));
        lr.setReason(req.getReason());
        return ResponseEntity.ok(LeaveRequestDTO.from(leaveService.create(lr)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestDTO> approve(@PathVariable Long id,
                                                   @RequestBody ApproveLeaveRequest req,
                                                   @RequestParam Long approverId) {
        boolean approved = "APPROVE".equalsIgnoreCase(req.getAction());
        LeaveRequest updated = leaveService.approve(id, approverId, approved, req.getRejectionReason());
        return ResponseEntity.ok(LeaveRequestDTO.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestParam Long empId) {
        leaveService.cancel(id, empId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance/{empId}")
    public ResponseEntity<LeaveBalanceDTO> getBalance(@PathVariable Long empId,
                                                      @RequestParam(defaultValue = "0") int year) {
        if (year == 0) year = LocalDate.now().getYear();
        final int finalYear = year;
        LeaveBalance lb = balanceRepo.findByEmployeeIdAndYear(empId, finalYear)
                .orElseThrow(() -> new ResourceNotFoundException("Không có thông tin ngày phép năm " + finalYear));
        return ResponseEntity.ok(LeaveBalanceDTO.from(lb));
    }
}
