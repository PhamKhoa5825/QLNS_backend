package com.example.qlns.Service;

import com.example.qlns.DTO.Response.EmployeeDetailDTO;
import com.example.qlns.DTO.Response.EmployeeSummaryDTO;
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

// =============================================
// TV1 - Dịch vụ Nhân viên
// =============================================
@Service
public class EmployeeService {
    private final EmployeeRepository empRepo;
    private final UserRepository userRepo;

    EmployeeService(EmployeeRepository empRepo, UserRepository userRepo) {
        this.empRepo = empRepo;
        this.userRepo = userRepo;
    }

    public List<Employee> getAll() {
        return empRepo.findAll();
    }

    public Employee getById(Long id) {
        return empRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
    }

    public List<Employee> getByDepartment(Long deptId) {
        return empRepo.findByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE);
    }

    public List<Employee> search(String keyword) {
        return empRepo.search(keyword);
    }

    @Transactional
    public Employee create(Employee emp, String email, String password, Role role) {
        if (userRepo.existsByEmail(email))
            throw new DuplicateException("Email đã được sử dụng: " + email);
        // Sẽ được mã hóa bằng PasswordEncoder
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
        String oldEmail = emp.getEmail();

        if (req.getFullName() != null) emp.setFullName(req.getFullName());
        if (req.getPhone() != null) emp.setPhone(req.getPhone());
        if (req.getAddress() != null) emp.setAddress(req.getAddress());
        if (req.getPosition() != null) emp.setPosition(req.getPosition());
        if (req.getDepartment() != null) emp.setDepartment(req.getDepartment());
        if (req.getAvatarUrl() != null) emp.setAvatarUrl(req.getAvatarUrl());
        
        if (req.getEmail() != null && !req.getEmail().equals(oldEmail)) {
            // Kiểm tra email mới đã tồn tại chưa
            if (userRepo.existsByEmail(req.getEmail())) {
                throw new DuplicateException("Email đã được sử dụng: " + req.getEmail());
            }
            emp.setEmail(req.getEmail());
            // Đồng bộ sang User
            userRepo.findByEmail(oldEmail).ifPresent(u -> {
                u.setEmail(req.getEmail());
                userRepo.save(u);
            });
        }
        
        return empRepo.save(emp);
    }

    @Transactional
    public void resign(Long id) {
        Employee emp = getById(id);
        emp.setStatus(EmployeeStatus.RESIGNED);
        empRepo.save(emp);
        // Vô hiệu hóa tài khoản
        userRepo.findByEmail(emp.getEmail()).ifPresent(u -> {
            u.setStatus(UserStatus.INACTIVE);
            userRepo.save(u);
        });
    }

    public String getRoleByEmployeeId(Long empId) {
        Employee emp = getById(empId);
        return userRepo.findByEmail(emp.getEmail())
                .map(u -> u.getRole().name())
                .orElse("EMPLOYEE");
    }

    // Lấy thông tin rút gọn (Trang chủ)
    public EmployeeSummaryDTO getEmployeeSummary(Long id) {
        Employee emp = getById(id);
        return new EmployeeSummaryDTO(emp.getFullName(), emp.getAvatarUrl());
    }

    // Lấy thông tin chi tiết (Trang cá nhân/Chỉnh sửa)
    public EmployeeDetailDTO getEmployeeDetail(Long id) {
        Employee emp = getById(id);
        String departmentName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : "Chưa có phòng ban";
        String role = getRoleByEmployeeId(id);

        return new EmployeeDetailDTO(
                emp.getId(),
                emp.getFullName(),
                emp.getAvatarUrl(),
                emp.getPosition(),
                departmentName,
                emp.getEmail(),
                emp.getPhone(),
                emp.getAddress(),
                role,
                emp.getDateOfBirth()
        );
    }
}
