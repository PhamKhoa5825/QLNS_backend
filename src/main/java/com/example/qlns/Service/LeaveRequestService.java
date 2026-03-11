package com.example.qlns.Service;

import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.LeaveRequest;
import com.example.qlns.Enum.LeaveStatus;
import com.example.qlns.Enum.LeaveType;
import com.example.qlns.Exception.ForbiddenException;
import com.example.qlns.Exception.LeaveRequestException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.LeaveBalanceRepository;
import com.example.qlns.Repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// =============================================
// TV3 - LeaveRequestService
// =============================================
@Service
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRepo;
    private final LeaveBalanceRepository balanceRepo;
    private final EmployeeRepository empRepo;

    LeaveRequestService(LeaveRequestRepository leaveRepo, LeaveBalanceRepository balanceRepo,
                        EmployeeRepository empRepo) {
        this.leaveRepo = leaveRepo;
        this.balanceRepo = balanceRepo;
        this.empRepo = empRepo;
    }

    public List<LeaveRequest> getByEmployee(Long empId) {
        return leaveRepo.findByEmployeeId(empId);
    }

    public List<LeaveRequest> getByDepartment(Long deptId) {
        return leaveRepo.findByDepartmentId(deptId);
    }

    public List<LeaveRequest> getPendingByDepartment(Long deptId) {
        return leaveRepo.findByDepartmentIdAndStatus(deptId, LeaveStatus.PENDING);
    }

    @Transactional
    public LeaveRequest create(LeaveRequest req) {
        // Kiểm tra còn phép không (chỉ với ANNUAL)
        if (req.getLeaveType() == LeaveType.ANNUAL) {
            int year = req.getStartDate().getYear();
            balanceRepo.findByEmployeeIdAndYear(req.getEmployee().getId(), year)
                    .ifPresent(lb -> {
                        if (lb.getRemainingDays() <= 0)
                            throw new LeaveRequestException("Hết ngày phép năm " + year);
                    });
        }
        return leaveRepo.save(req);
    }

    @Transactional
    public LeaveRequest approve(Long id, Long approverId, boolean approved, String rejectionReason) {
        LeaveRequest req = leaveRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn id=" + id));
        if (req.getStatus() != LeaveStatus.PENDING)
            throw new LeaveRequestException("Đơn đã được xử lý rồi");

        Employee approver = empRepo.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người duyệt"));

        req.setApprovedBy(approver);
        if (approved) {
            req.setStatus(LeaveStatus.APPROVED);
            // Trừ ngày phép nếu là ANNUAL
            if (req.getLeaveType() == LeaveType.ANNUAL) {
                int year = req.getStartDate().getYear();
                long days = req.getStartDate().datesUntil(req.getEndDate().plusDays(1)).count();
                balanceRepo.findByEmployeeIdAndYear(req.getEmployee().getId(), year)
                        .ifPresent(lb -> {
                            lb.setUsedDays(lb.getUsedDays() + (int) days);
                            lb.setRemainingDays(lb.getTotalDays() - lb.getUsedDays());
                            balanceRepo.save(lb);
                        });
            }
        } else {
            req.setStatus(LeaveStatus.REJECTED);
            req.setRejectionReason(rejectionReason);
        }
        return leaveRepo.save(req);
    }

    @Transactional
    public void cancel(Long id, Long empId) {
        LeaveRequest req = leaveRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        if (!req.getEmployee().getId().equals(empId))
            throw new ForbiddenException("Không có quyền hủy đơn này");
        if (req.getStatus() != LeaveStatus.PENDING)
            throw new LeaveRequestException("Chỉ hủy được đơn đang chờ duyệt");
        leaveRepo.delete(req);
    }
}
