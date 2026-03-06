package com.example.qlns.Service;

import com.example.qlns.Entity.*;
import com.example.qlns.Enum.Role;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

// =============================================
// 1. DEPARTMENT SERVICE (TV1 viết)
// =============================================
@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng ban", id));
    }

    public Department create(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new DuplicateException("Tên phòng ban", department.getName());
        }
        return departmentRepository.save(department);
    }

    public Department update(Long id, Department updated) {
        Department existing = getById(id);
        if (!existing.getName().equals(updated.getName())
                && departmentRepository.existsByName(updated.getName())) {
            throw new DuplicateException("Tên phòng ban", updated.getName());
        }
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return departmentRepository.save(existing);
    }

    public void delete(Long id) {
        Department department = getById(id);
        if (!department.getEmployees().isEmpty()) {
            throw new BadRequestException("Không thể xóa phòng ban đang có nhân viên");
        }
        departmentRepository.deleteById(id);
    }

    public Department setManager(Long departmentId, Long employeeId) {
        Department department = getById(departmentId);
        Employee manager = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", employeeId));

        if (manager.getUser().getRole() != Role.MANAGER
                && manager.getUser().getRole() != Role.ADMIN) {
            throw new BadRequestException("Nhân viên này không có quyền làm quản lý");
        }
        department.setManager(manager);
        return departmentRepository.save(department);
    }

    public @Nullable List<Employee> getEmployeesByDepartment(Long id) {
        return null;
    }

    public int countEmployees(Long departmentId) {
        return employeeRepository.findByDepartmentIdAndEndDateIsNull(departmentId).size();
    }
}
