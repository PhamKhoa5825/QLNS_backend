package com.example.qlns.Service;

import com.example.qlns.Entity.Task;
import com.example.qlns.Entity.TaskUpdate;
import com.example.qlns.Enum.TaskStatus;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.EmployeeRepository;
import com.example.qlns.Repository.TaskRepository;
import com.example.qlns.Repository.TaskUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// =============================================
// TV3 - TaskService
// =============================================
@Service
public class TaskService {
    private final TaskRepository taskRepo;
    private final TaskUpdateRepository taskUpdateRepo;
    private final EmployeeRepository empRepo;

    TaskService(TaskRepository taskRepo, TaskUpdateRepository taskUpdateRepo,
                EmployeeRepository empRepo) {
        this.taskRepo = taskRepo;
        this.taskUpdateRepo = taskUpdateRepo;
        this.empRepo = empRepo;
    }

    public List<Task> getMyTasks(Long empId) {
        return taskRepo.findByAssignedToId(empId);
    }

    public List<Task> getByDepartment(Long deptId) {
        return taskRepo.findByDepartmentId(deptId);
    }

    public Task getById(Long id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhiệm vụ id=" + id));
    }

    @Transactional
    public Task create(Task task) {
        return taskRepo.save(task);
    }

    @Transactional
    public Task updateStatus(Long taskId, TaskStatus newStatus, String note, Long updatedById) {
        Task task = getById(taskId);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE) task.setCompletedAt(LocalDateTime.now());
        taskRepo.save(task);

        // Lưu lịch sử
        TaskUpdate update = new TaskUpdate();
        update.setTask(task);
        update.setStatus(newStatus);
        update.setNote(note);
        empRepo.findById(updatedById).ifPresent(update::setUpdatedBy);
        taskUpdateRepo.save(update);

        return task;
    }

    @Transactional
    public void delete(Long id) {
        taskRepo.deleteById(id);
    }

    public List<TaskUpdate> getHistory(Long taskId) {
        return taskUpdateRepo.findByTaskIdOrderByUpdatedAtDesc(taskId);
    }
}
