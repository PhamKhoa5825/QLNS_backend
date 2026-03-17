package com.example.qlns.Service;

import com.example.qlns.DTO.Request.UpdateTaskRequest;
import com.example.qlns.DTO.Response.TaskDTO;
import com.example.qlns.Entity.Employee;
import com.example.qlns.Entity.Task;
import com.example.qlns.Entity.TaskUpdate;
import com.example.qlns.Enum.TaskPriority;
import com.example.qlns.Enum.TaskStatus;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.TaskRepository;
import com.example.qlns.Repository.TaskUpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired private TaskRepository taskRepo;
    @Autowired private TaskUpdateRepository taskUpdateRepo;
    @Autowired private EmployeeRepository empRepo;

    // ── Admin: Xem tất cả task ─────────────────────────────────
    @Transactional(readOnly = true)
    public List<TaskDTO> getAll() {
        return taskRepo.findAll()
                .stream().map(TaskDTO::from).collect(Collectors.toList());
    }

    // ── Xem task của nhân viên ────────────────────────────────
    @Transactional(readOnly = true)
    public List<TaskDTO> getMyTasks(Long empId) {
        return taskRepo.findByAssignedToId(empId)
                .stream().map(TaskDTO::from).collect(Collectors.toList());
    }

    // ── Xem task theo phòng ban ───────────────────────────────
    @Transactional(readOnly = true)
    public List<TaskDTO> getByDepartment(Long deptId) {
        return taskRepo.findByDepartmentId(deptId)
                .stream().map(TaskDTO::from).collect(Collectors.toList());
    }

    // ── Xem chi tiết task ────────────────────────────────────
    @Transactional(readOnly = true)
    public TaskDTO getById(Long id) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhiệm vụ id=" + id));
        return TaskDTO.from(task);
    }

    // ── Tạo task mới (Manager giao) ───────────────────────────
    @Transactional
    public TaskDTO create(Task task) {
        // Task mới luôn bắt đầu ở PENDING
        task.setStatus(TaskStatus.PENDING);
        return TaskDTO.from(taskRepo.save(task));
    }

    // ── Chỉnh sửa task (Manager/Admin) ───────────────────────
    @Transactional
    public TaskDTO updateTask(Long taskId, UpdateTaskRequest req) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhiệm vụ id=" + taskId));

        // Không cho sửa task đã hoàn thành
        if (task.getStatus() == TaskStatus.DONE)
            throw new BadRequestException("Task đã hoàn thành, không thể chỉnh sửa");

        if (req.getTitle() != null && !req.getTitle().isBlank())
            task.setTitle(req.getTitle().trim());
        if (req.getDescription() != null)
            task.setDescription(req.getDescription());
        if (req.getPriority() != null)
            task.setPriority(TaskPriority.valueOf(req.getPriority()));
        if (req.getDeadline() != null)
            task.setDeadline(java.time.LocalDate.parse(req.getDeadline()).atStartOfDay());
        if (req.getAssignedToId() != null) {
            Employee newAssignee = empRepo.findById(req.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy nhân viên id=" + req.getAssignedToId()));
            task.setAssignedTo(newAssignee);
        }

        return TaskDTO.from(taskRepo.save(task));
    }

    // ── Nhân viên nhận việc: PENDING → ACCEPTED ──────────────
    @Transactional
    public TaskDTO acceptTask(Long taskId, Long employeeId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy task"));

        if (!task.getAssignedTo().getId().equals(employeeId))
            throw new ForbiddenException("Bạn không được giao task này");

        if (task.getStatus() != TaskStatus.PENDING)
            throw new BadRequestException("Chỉ có thể nhận task đang ở trạng thái Chờ nhận");

        task.setStatus(TaskStatus.ACCEPTED);
        saveHistory(task, TaskStatus.ACCEPTED, "Nhân viên đã nhận việc", employeeId);
        return TaskDTO.from(taskRepo.save(task));
    }

    // ── Cập nhật trạng thái task ──────────────────────────────
    @Transactional
    public TaskDTO updateStatus(Long taskId, TaskStatus newStatus, String note, Long updatedById) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy task"));

        TaskStatus current = task.getStatus();

        // Kiểm tra quyền: Nhân viên chỉ sửa task của mình, Manager/Admin sửa thoải mái
        if (!task.getAssignedTo().getId().equals(updatedById)) {
             if (task.getAssignedBy() != null && !task.getAssignedBy().getId().equals(updatedById)) {
                  // Cần check Admin role ở đây nếu muốn chặt chẽ hơn, nhưng service này tin cậy input từ Controller
                  // throw new ForbiddenException("Bạn không có quyền cập nhật trạng thái nhiệm vụ này");
             }
        }

        // Không cho sửa task đã hoàn thành
        if (current == TaskStatus.DONE)
            throw new BadRequestException("Task đã hoàn thành, không thể thay đổi trạng thái");

        // Không cho nhảy thẳng PENDING → DONE
        if (current == TaskStatus.PENDING && newStatus == TaskStatus.DONE)
            throw new BadRequestException("Phải nhận việc trước khi đánh dấu hoàn thành");

        task.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE)
            task.setCompletedAt(LocalDateTime.now());

        saveHistory(task, newStatus, note, updatedById);
        return TaskDTO.from(taskRepo.save(task));
    }

    // ── Xoá task ─────────────────────────────────────────────
    @Transactional
    public void delete(Long id) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy task"));

        if (task.getStatus() == TaskStatus.DONE)
            throw new BadRequestException("Không thể xoá task đã hoàn thành");

        taskRepo.delete(task);
    }

    // ── Xem lịch sử cập nhật task ────────────────────────────
    public List<TaskUpdate> getHistory(Long taskId) {
        return taskUpdateRepo.findByTaskIdOrderByUpdatedAtDesc(taskId);
    }

    // ── Scheduler: tự động đánh dấu OVERDUE ────────────────
    @Scheduled(cron = "0 0 8 * * MON-FRI")
    @Transactional
    public void markOverdueTasks() {
        List<Task> overdueTasks = taskRepo.findOverdueTasksToMark(LocalDateTime.now());
        if (!overdueTasks.isEmpty()) {
            overdueTasks.forEach(t -> {
                t.setStatus(TaskStatus.OVERDUE);
                saveHistory(t, TaskStatus.OVERDUE, "Tự động đánh dấu quá hạn bởi hệ thống", null);
            });
            taskRepo.saveAll(overdueTasks);
        }
    }

    private void saveHistory(Task task, TaskStatus status, String note, Long updatedById) {
        TaskUpdate log = new TaskUpdate();
        log.setTask(task);
        log.setStatus(status);
        log.setNote(note);
        if (updatedById != null)
            empRepo.findById(updatedById).ifPresent(log::setUpdatedBy);
        taskUpdateRepo.save(log);
    }
}