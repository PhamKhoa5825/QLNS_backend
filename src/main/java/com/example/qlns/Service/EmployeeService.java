package com.example.qlns.Service;

import com.example.qlns.Entity.*;
import com.example.qlns.Enum.Role;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<Employee> getAll() {
        return employeeRepository.findByEndDateIsNull();
    }

    public List<Employee> getByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentIdAndEndDateIsNull(departmentId);
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", id));
    }

    public List<Employee> search(String keyword) {
        return employeeRepository.findByFullNameContainingIgnoreCase(keyword);
    }

    // Tạo nhân viên mới: tạo User (account) + Employee (thông tin) cùng lúc
    @Transactional
    public Employee create(Employee employee, String email, String rawPassword) {
        // Kiểm tra email trùng
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateException("Email", email);
        }
        // Kiểm tra mã NV trùng
        if (employeeRepository.existsByEmployeeCode(employee.getEmployeeCode())) {
            throw new DuplicateException("Mã nhân viên", employee.getEmployeeCode());
        }
        // Kiểm tra CCCD trùng
        if (employee.getNationalId() != null
                && employeeRepository.existsByNationalId(employee.getNationalId())) {
            throw new DuplicateException("CCCD", employee.getNationalId());
        }

        // TV2 sẽ inject PasswordEncoder, ở đây để comment gợi ý
        // String hashedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(email, rawPassword, Role.EMPLOYEE);
        userRepository.save(user);

        employee.setUser(user);
        return employeeRepository.save(employee);
    }

    public Employee update(Long id, Employee updated) {
        Employee existing = getById(id);
        existing.setFullName(updated.getFullName());
        existing.setPhone(updated.getPhone());
        existing.setPosition(updated.getPosition());
        existing.setAddress(updated.getAddress());
        existing.setGender(updated.getGender());
        existing.setBirthDate(updated.getBirthDate());
        existing.setSalary(updated.getSalary());
        existing.setContractType(updated.getContractType());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setAvatarUrl(updated.getAvatarUrl());

        if (updated.getDepartment() != null) {
            Department dept = departmentRepository.findById(updated.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng ban",
                            updated.getDepartment().getId()));
            existing.setDepartment(dept);
        }
        return employeeRepository.save(existing);
    }

    // Nghỉ việc: set endDate thay vì xóa
    public void terminate(Long id) {
        Employee employee = getById(id);
        employee.setEndDate(LocalDate.now());
        employee.getUser().setActive(false);
        employeeRepository.save(employee);
    }
}

