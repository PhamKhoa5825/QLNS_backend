package com.example.qlns.Service;

import com.example.qlns.DTO.Response.EmployeeDTO;
import com.example.qlns.DTO.Response.EmployeeDetailDTO;
import com.example.qlns.DTO.Response.EmployeeSummaryDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.User;
import com.example.qlns.Enum.EmployeeStatus;
import com.example.qlns.Enum.Role;
import com.example.qlns.Enum.UserStatus;
import com.example.qlns.Exception.DuplicateException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.DepartmentRepository;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.TaskRepository;
import com.example.qlns.Repository.UserRepository;
import com.example.qlns.Entity.Task;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    private final EmployeeRepository empRepo;
    private final UserRepository userRepo;

    private final TaskRepository taskRepo;
    private final DepartmentRepository deptRepo;
    private final PasswordEncoder passwordEncoder;
    private final SystemLogService logService;

    EmployeeService(EmployeeRepository empRepo, UserRepository userRepo, TaskRepository taskRepo, DepartmentRepository deptRepo, PasswordEncoder passwordEncoder, SystemLogService logService) {
        this.empRepo = empRepo;
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
        this.deptRepo = deptRepo;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAll() {
        return empRepo.findAllWithDept().stream()
                .map(emp -> EmployeeDTO.from(emp, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Employee getById(Long id) {
        return empRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên id=" + id));
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getByDepartment(Long deptId) {
        return empRepo.findByDepartmentIdAndStatus(deptId, EmployeeStatus.ACTIVE).stream()
                .map(emp -> EmployeeDTO.from(emp, getRoleByEmployeeId(emp.getId())))
                .collect(Collectors.toList());
    }

    public List<Employee> search(String keyword) {
        return empRepo.search(keyword);
    }

    @Transactional
    public Employee create(Employee emp, String email, String password, Role role) {
        if (userRepo.existsByEmail(email))
            throw new DuplicateException("Email đã được sử dụng: " + email);
        
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email.split("@")[0], email, encodedPassword, role);
        user = userRepo.save(user);

        emp.setEmail(email);
        Employee saved = empRepo.save(emp);

        user.setEmployeeId(saved.getId());
        userRepo.save(user);

        // Log activity
        logService.log("CREATE", "Đã tạo nhân viên mới: " + saved.getFullName() + " (" + email + ")");

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
        Employee saved = empRepo.save(emp);
        
        // Log activity
        logService.log("UPDATE", "Cập nhật thông tin nhân viên: " + saved.getFullName());
        
        return saved;
    }

    @Transactional
    public void resign(Long id) {
        Employee emp = getById(id);
        
        // 1. Đổi status NV
        emp.setStatus(EmployeeStatus.RESIGNED);
        empRepo.save(emp);
        
        // 2. Task PENDING/ACCEPTED -> OVERDUE
        List<Task> pendingTasks = taskRepo.findByAssignedToIdAndStatus(id, com.example.qlns.Enum.TaskStatus.PENDING);
        List<Task> acceptedTasks = taskRepo.findByAssignedToIdAndStatus(id, com.example.qlns.Enum.TaskStatus.ACCEPTED);
        
        for (com.example.qlns.Entity.Task task : pendingTasks) {
            task.setStatus(com.example.qlns.Enum.TaskStatus.OVERDUE);
            taskRepo.save(task);
        }
        for (com.example.qlns.Entity.Task task : acceptedTasks) {
            task.setStatus(com.example.qlns.Enum.TaskStatus.OVERDUE);
            taskRepo.save(task);
        }

        // 3. Gỡ Manager khỏi PB
        deptRepo.findByManagerId(id).ifPresent(dept -> {
            dept.setManager(null);
            deptRepo.save(dept);
        });

        // 4. Khóa tài khoản
        userRepo.findByEmail(emp.getEmail()).ifPresent(u -> {
            u.setStatus(UserStatus.INACTIVE);
            userRepo.save(u);
        });

        // Log activity
        logService.log("DELETE", "Nhân viên " + emp.getFullName() + " nghỉ việc (Đã khóa tài khoản)");
    }

    /**
     * Kiểm tra ảnh hưởng trước khi nghỉ việc
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> checkResignImpact(Long id) {
        Employee emp = getById(id);
        java.util.Map<String, Object> impact = new java.util.HashMap<>();
        impact.put("employeeId", emp.getId());
        impact.put("employeeName", emp.getFullName());

        long pendingTasks = taskRepo.countByAssignedToIdAndStatus(id, com.example.qlns.Enum.TaskStatus.PENDING);
        long acceptedTasks = taskRepo.countByAssignedToIdAndStatus(id, com.example.qlns.Enum.TaskStatus.ACCEPTED);
        impact.put("pendingTasks", pendingTasks);
        impact.put("acceptedTasks", acceptedTasks);

        boolean isManager = deptRepo.findByManagerId(id).isPresent();
        impact.put("isManager", isManager);

        return impact;
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

        // Log activity
        logService.log("UPDATE", "Kích hoạt lại nhân viên: " + emp.getFullName());
    }

    public String getRoleByEmployeeId(Long empId) {
        Employee emp = getById(empId); 
        return userRepo.findByEmail(emp.getEmail())
                .map(u -> u.getRole().name())
                .orElse("EMPLOYEE");
    }

    /**
     * Lấy thông tin rút gọn (Trang chủ Employee App)
     */
    public EmployeeSummaryDTO getEmployeeSummary(Long id) {
        Employee emp = getById(id);
        return new EmployeeSummaryDTO(emp.getFullName(), emp.getAvatarUrl());
    }

    /**
     * Lấy thông tin chi tiết (Trang cá nhân/Chỉnh sửa Employee App)
     */
    public EmployeeDetailDTO getEmployeeDetail(Long id) {
        Employee emp = getById(id);
        String departmentName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : "Chưa có phòng ban";
        
        // Inline role retrieval to avoid redundant DB call if needed, but keeping it clean
        String role = userRepo.findByEmail(emp.getEmail())
                .map(u -> u.getRole().name())
                .orElse("EMPLOYEE");

        EmployeeDetailDTO dto = new EmployeeDetailDTO(
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
        
        Double quota = emp.getAnnualLeaveQuota() != null ? emp.getAnnualLeaveQuota() : 12.0;
        Double used = emp.getLeaveDaysUsed() != null ? emp.getLeaveDaysUsed() : 0.0;
        dto.setAnnualLeaveQuota(quota);
        dto.setLeaveDaysUsed(used);
        dto.setRemainingLeave(quota - used);
        
        return dto;
    }
}
