package com.example.qlns.Service;

import com.example.qlns.Entity.*;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

// =============================================
// 4. TASK SERVICE (TV3 viết)
// =============================================
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    public TaskService(TaskRepository taskRepository,
                       EmployeeRepository employeeRepository) {
        this.taskRepository = taskRepository;
        this.employeeRepository = employeeRepository;
    }

    public Task create(Task task, Long createdById, Long assigneeId) {
        Employee createdBy = employeeRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", createdById));
        Employee assignee = employeeRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", assigneeId));
        task.setCreatedBy(createdBy);
        task.setAssignee(assignee);
        return taskRepository.save(task);
    }

    public List<Task> getMyTasks(Long employeeId) {
        return taskRepository.findByAssigneeIdOrderByCreatedAtDesc(employeeId);
    }

    public Task updateStatus(Long taskId, Long employeeId, Task.TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        if (!task.getAssignee().getId().equals(employeeId)) {
            throw new ForbiddenException("Bạn không phải người được giao task này");
        }
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    public void delete(Long taskId, Long employeeId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        if (!task.getCreatedBy().getId().equals(employeeId)) {
            throw new ForbiddenException("Chỉ người tạo task mới có thể xóa");
        }
        taskRepository.deleteById(taskId);
    }
}
