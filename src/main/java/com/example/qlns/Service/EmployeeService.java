package com.example.qlns.Service;

import com.example.qlns.DTO.Response.EmployeeDTO;
import com.example.qlns.Entity.*;
import com.example.qlns.Enum.*;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EmployeeService v2 — Thêm resign cascade + validate role
 *
 * Thay đổi so với bản cũ:
 * 1. resign() giờ cascade: OVERDUE task pending, gỡ Manager PB, khóa TK
 * 2. resignWithInfo() trả về thông tin chi tiết cho FE hiển thị warning
 * 3. updateRole() thêm validate conflict Manager
 */
@Service
public class EmployeeService {
    private final EmployeeRepository empRepo;
    private final UserRepository userRepo;
    private final TaskRepository taskRepo;
    private final DepartmentRepository deptRepo;
    private final SystemLogService logService;

    EmployeeService(EmployeeRepository empRepo,
                    UserRepository userRepo,
                    TaskRepository taskRepo,
                    DepartmentRepository deptRepo,
                    SystemLogService logService) {
        this.empRepo = empRepo;
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
        this.deptRepo = deptRepo;
        this.logService = logService;
    }

    // ══════════════════════════════════════════════════════════
    //  ĐỌC — giữ nguyên logic cũ
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Employee> getAll() { return empRepo.findAll(); }

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

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllDTO() {
        return empRepo.findAll().stream().map(this::buildDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getByIdDTO(Long id) {
        return buildDTO(getById(id));
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getByDepartmentDTO(Long deptId) {
        return getByDepartment(deptId).stream().map(this::buildDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> searchDTO(String keyword) {
        return search(keyword).stream().map(this::buildDTO).collect(Collectors.toList());
    }

    private EmployeeDTO buildDTO(Employee emp) {
        return userRepo.findByEmail(emp.getEmail())
                .map(user -> EmployeeDTO.from(emp,
                        user.getRole().name(),
                        user.getId(),
                        user.getStatus().name()))
                .orElse(EmployeeDTO.from(emp, "EMPLOYEE"));
    }

    // ══════════════════════════════════════════════════════════
    //  TẠO + CẬP NHẬT — giữ nguyên logic cũ
    // ══════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════
    //  MỚI: Kiểm tra trước khi nghỉ việc (FE gọi trước để hiện warning)
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/employees/{id}/resign-check
     * Trả về thông tin ảnh hưởng nếu cho NV nghỉ việc.
     * FE dùng để hiện dialog cảnh báo trước khi xác nhận.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkResignImpact(Long id) {
        Employee emp = getById(id);
        Map<String, Object> impact = new HashMap<>();
        impact.put("employeeId", emp.getId());
        impact.put("employeeName", emp.getFullName());

        // Task đang pending/accepted
        long pendingTasks = taskRepo.countByAssignedToIdAndStatus(id, TaskStatus.PENDING);
        long acceptedTasks = taskRepo.countByAssignedToIdAndStatus(id, TaskStatus.ACCEPTED);
        impact.put("pendingTasks", pendingTasks);
        impact.put("acceptedTasks", acceptedTasks);

        // NV có đang là Manager của PB nào không?
        List<Department> managedDepts = deptRepo.findByManagerId(id);
        impact.put("managedDepartments", managedDepts.stream()
                .map(d -> Map.of("id", d.getId(), "name", d.getName()))
                .collect(Collectors.toList()));
        impact.put("isManager", !managedDepts.isEmpty());

        return impact;
    }

    // ══════════════════════════════════════════════════════════
    //  CẢI THIỆN: Nghỉ việc có cascade
    // ══════════════════════════════════════════════════════════

    /**
     * Cho NV nghỉ việc + cascade:
     * 1. Đổi status = RESIGNED
     * 2. Task PENDING/ACCEPTED -> OVERDUE
     * 3. Gỡ Manager khỏi PB (nếu NV đang là Manager)
     * 4. Khóa tài khoản (INACTIVE)
     *
     * Trả về Map chứa thông tin đã xử lý (FE hiển thị kết quả).
     */
    @Transactional
    public Map<String, Object> resignWithCascade(Long id) {
        Employee emp = getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", emp.getId());
        result.put("employeeName", emp.getFullName());

        // 1. Đổi status NV
        emp.setStatus(EmployeeStatus.RESIGNED);
        empRepo.save(emp);

        // 2. Task PENDING/ACCEPTED -> OVERDUE
        List<Task> pendingTasks = taskRepo.findByAssignedToIdAndStatus(id, TaskStatus.PENDING);
        List<Task> acceptedTasks = taskRepo.findByAssignedToIdAndStatus(id, TaskStatus.ACCEPTED);
        int taskCount = 0;
        for (Task task : pendingTasks) {
            task.setStatus(TaskStatus.OVERDUE);
            taskRepo.save(task);
            taskCount++;
        }
        for (Task task : acceptedTasks) {
            task.setStatus(TaskStatus.OVERDUE);
            taskRepo.save(task);
            taskCount++;
        }
        result.put("tasksMarkedOverdue", taskCount);

        // 3. Gỡ Manager khỏi PB
        List<Department> managedDepts = deptRepo.findByManagerId(id);
        for (Department dept : managedDepts) {
            dept.setManager(null);
            deptRepo.save(dept);
        }
        result.put("departmentsUnmanaged", managedDepts.stream()
                .map(Department::getName).collect(Collectors.toList()));

        // 4. Khóa tài khoản
        userRepo.findByEmail(emp.getEmail()).ifPresent(u -> {
            u.setStatus(UserStatus.INACTIVE);
            userRepo.save(u);
        });
        result.put("accountLocked", true);

        return result;
    }

    /**
     * Giữ method resign() cũ cho tương thích ngược.
     * Gọi resignWithCascade() bên trong.
     */
    @Transactional
    public void resign(Long id) {
        resignWithCascade(id);
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

    // ══════════════════════════════════════════════════════════
    //  CẢI THIỆN: Đổi role có validate
    // ══════════════════════════════════════════════════════════

    /**
     * Đổi role NV + validate:
     * - Nếu đổi sang MANAGER: check NV có thuộc PB không, PB đó đã có Manager chưa
     * - Nếu hạ từ MANAGER: cảnh báo PB sẽ mất trưởng phòng
     */
    @Transactional
    public EmployeeDTO updateRole(Long employeeId, String newRole) {
        Employee emp = getById(employeeId);
        Role targetRole;
        try {
            targetRole = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Role không hợp lệ: " + newRole);
        }

        // Validate khi đổi sang MANAGER
        if (targetRole == Role.MANAGER) {
            if (emp.getDepartment() == null) {
                throw new BadRequestException(
                        "Nhân viên " + emp.getFullName() + " chưa thuộc phòng ban nào. "
                                + "Vui lòng phân công phòng ban trước khi đổi quyền Manager.");
            }
            // Check PB đã có Manager khác chưa
            Department dept = emp.getDepartment();
            if (dept.getManager() != null && !dept.getManager().getId().equals(emp.getId())) {
                throw new BadRequestException(
                        "Phòng ban " + dept.getName() + " đã có trưởng phòng: "
                                + dept.getManager().getFullName()
                                + ". Vui lòng gỡ trưởng phòng cũ trước.");
            }
        }

        // Thực hiện đổi role
        userRepo.findByEmail(emp.getEmail()).ifPresent(u -> {
            u.setRole(targetRole);
            userRepo.save(u);
        });

        // Nếu đổi sang MANAGER và PB chưa có Manager -> tự gán luôn
        if (targetRole == Role.MANAGER && emp.getDepartment() != null) {
            Department dept = emp.getDepartment();
            if (dept.getManager() == null) {
                dept.setManager(emp);
                deptRepo.save(dept);
            }
        }

        return buildDTO(emp);
    }
}
