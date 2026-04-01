package com.example.qlns.Service;

import com.example.qlns.Entity.*;
import com.example.qlns.Enum.*;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// =============================================
// TV1 - DepartmentService
// =============================================
@Service
public class DepartmentService {
    private final DepartmentRepository deptRepo;
    private final EmployeeRepository empRepo;
    private final SystemLogService logService;

    DepartmentService(DepartmentRepository deptRepo, EmployeeRepository empRepo, SystemLogService logService) {
        this.deptRepo = deptRepo;
        this.empRepo = empRepo;
        this.logService = logService;
    }

    public List<Department> getAll() { return deptRepo.findAll(); }

    public Department getById(Long id) {
        return deptRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng ban id=" + id));
    }

    @Transactional
    public Department create(Department dept) {
        if (deptRepo.existsByName(dept.getName()))
            throw new DuplicateException("Tên phòng ban đã tồn tại: " + dept.getName());
        Department saved = deptRepo.save(dept);
        logService.log("CREATE", "Đã tạo phòng ban mới: " + saved.getName());
        return saved;
    }

    @Transactional
    public Department update(Long id, Department req) {
        Department dept = getById(id);
        dept.setName(req.getName());
        dept.setDescription(req.getDescription());
        Department saved = deptRepo.save(dept);
        logService.log("UPDATE", "Cập nhật thông tin phòng ban: " + saved.getName());
        return saved;
    }

    @Transactional
    public Department setManager(Long deptId, Long empId) {
        Department dept = getById(deptId);
        Employee emp = empRepo.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + empId));
        dept.setManager(emp);
        Department saved = deptRepo.save(dept);
        logService.log("UPDATE", "Đã chỉ định " + emp.getFullName() + " làm Trưởng phòng " + saved.getName());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Department dept = getById(id);
        long count = empRepo.countByDepartmentIdAndStatus(id, EmployeeStatus.ACTIVE);
        if (count > 0) throw new BadRequestException("Phòng ban còn " + count + " nhân viên đang làm việc");
        deptRepo.delete(dept);
        logService.log("DELETE", "Đã xóa phòng ban: " + dept.getName());
    }

    public int countEmployees(Long deptId) {
        return (int) empRepo.countByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE);
    }
}

