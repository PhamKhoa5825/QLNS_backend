package com.example.qlns.Service;

import com.example.qlns.DTO.Response.EmployeeDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.User;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Enum.Role;
import com.example.qlns.Enum.UserStatus;
import com.example.qlns.Exception.DuplicateException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository empRepo;
    private final UserRepository userRepo;

    EmployeeService(EmployeeRepository empRepo, UserRepository userRepo) {
        this.empRepo = empRepo;
        this.userRepo = userRepo;
    }

    // ── ĐỌC (có @Transactional để LAZY không crash) ──────────

    @Transactional(readOnly = true)
    public List<Employee> getAll() {
        return empRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return empRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Employee> getByDepartment(Long deptId) {
        return empRepo.findByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Employee> search(String keyword) {
        return empRepo.search(keyword);
    }

    @Transactional(readOnly = true)
    public String getRoleByEmployeeId(Long empId) {
        Employee emp = getById(empId);
        return userRepo.findByEmail(emp.getEmail())
                .map(u -> u.getRole().name())
                .orElse("EMPLOYEE");
    }

    /**
     * Trả về DTO list — gọi EmployeeDTO.from() BÊN TRONG transaction
     * → emp.getDepartment() (LAZY) không crash.
     * Kèm userId + accountStatus từ User table.
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllDTO() {
        return empRepo.findAll().stream()
                .map(this::buildDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getByIdDTO(Long id) {
        return buildDTO(getById(id));
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getByDepartmentDTO(Long deptId) {
        return getByDepartment(deptId).stream()
                .map(this::buildDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> searchDTO(String keyword) {
        return search(keyword).stream()
                .map(this::buildDTO)
                .collect(Collectors.toList());
    }

    /** Helper: build DTO với đầy đủ userId, role, accountStatus */
    private EmployeeDTO buildDTO(Employee emp) {
        return userRepo.findByEmail(emp.getEmail())
                .map(user -> EmployeeDTO.from(emp,
                        user.getRole().name(),
                        user.getId(),
                        user.getStatus().name()))
                .orElse(EmployeeDTO.from(emp, "EMPLOYEE"));
    }

    // ── GHI ───────────────────────────────────────────────────

    @Transactional
    public Employee create(Employee emp, String email, String password, Role role) {
        if (userRepo.existsByEmail(email))
            throw new DuplicateException("Email đã được sử dụng: " + email);

        User user = new User(email.split("@")[0], email, password, role);
        user = userRepo.save(user);

        emp.setEmail(email);
        Employee saved = empRepo.save(emp);

        user.setEmployeeId(saved.getId());
        userRepo.save(user);

        return saved;
    }

    @Transactional
    public Employee update(Long id, Employee req) {
        Employee emp = getById(id);
        if (req.getFullName() != null) emp.setFullName(req.getFullName());
        if (req.getPhone() != null) emp.setPhone(req.getPhone());
        if (req.getAddress() != null) emp.setAddress(req.getAddress());
        if (req.getPosition() != null) emp.setPosition(req.getPosition());
        if (req.getDepartment() != null) emp.setDepartment(req.getDepartment());
        if (req.getAvatarUrl() != null) emp.setAvatarUrl(req.getAvatarUrl());
        return empRepo.save(emp);
    }

    @Transactional
    public void resign(Long id) {
        Employee emp = getById(id);
        emp.setStatus(EmployeeStatus.RESIGNED);
        empRepo.save(emp);
        userRepo.findByEmail(emp.getEmail()).ifPresent(u -> {
            u.setStatus(UserStatus.INACTIVE);
            userRepo.save(u);
        });
    }

    @Transactional
    public void reactivate(Long id) {
        Employee emp = getById(id);
        emp.setStatus(EmployeeStatus.ACTIVE);
        empRepo.save(emp);
        userRepo.findByEmail(emp.getEmail()).ifPresent(u -> {
            u.setStatus(UserStatus.ACTIVE);
            userRepo.save(u);
        });
    }

    @Transactional
    public EmployeeDTO updateRole(Long employeeId, String newRole) {
        Employee emp = getById(employeeId);
        userRepo.findByEmail(emp.getEmail()).ifPresent(u -> {
            try {
                u.setRole(Role.valueOf(newRole.toUpperCase()));
                userRepo.save(u);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid Role: " + newRole);
            }
        });
        return buildDTO(emp);
    }
}