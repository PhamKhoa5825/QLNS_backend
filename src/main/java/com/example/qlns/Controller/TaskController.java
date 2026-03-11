package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateTaskRequest;
import com.example.qlns.DTO.Request.UpdateTaskStatusRequest;
import com.example.qlns.DTO.Response.TaskDTO;
import com.example.qlns.Entity.Task;
import com.example.qlns.Entity.TaskUpdate;
import com.example.qlns.Enum.TaskPriority;
import com.example.qlns.Enum.TaskStatus;
import com.example.qlns.Service.EmployeeService;
import com.example.qlns.Service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// =============================================
// TV3 - TaskController
// =============================================
@RestController
@RequestMapping("/api/tasks")
class TaskController {
    private final TaskService taskService;
    private final EmployeeService empService;

    TaskController(TaskService taskService, EmployeeService empService) {
        this.taskService = taskService;
        this.empService = empService;
    }

    @GetMapping("/my/{empId}")
    public ResponseEntity<List<TaskDTO>> getMyTasks(@PathVariable Long empId) {
        return ResponseEntity.ok(taskService.getMyTasks(empId).stream()
                .map(TaskDTO::from).collect(Collectors.toList()));
    }

    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<TaskDTO>> getByDepartment(@PathVariable Long deptId) {
        return ResponseEntity.ok(taskService.getByDepartment(deptId).stream()
                .map(TaskDTO::from).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TaskDTO.from(taskService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<TaskDTO> create(@RequestBody CreateTaskRequest req) {
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setPriority(TaskPriority.valueOf(req.getPriority()));
        if (req.getDeadline() != null) task.setDeadline(LocalDate.parse(req.getDeadline()).atStartOfDay());
        task.setAttachmentUrl(req.getAttachmentUrl());
        empService.getById(req.getAssignedToId()).equals(null);  // validate
        task.setAssignedTo(empService.getById(req.getAssignedToId()));
        return ResponseEntity.ok(TaskDTO.from(taskService.create(task)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateStatus(@PathVariable Long id,
                                                @RequestBody UpdateTaskStatusRequest req,
                                                @RequestParam Long updatedById) {
        Task updated = taskService.updateStatus(id, TaskStatus.valueOf(req.getStatus()),
                req.getNote(), updatedById);
        return ResponseEntity.ok(TaskDTO.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TaskUpdate>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getHistory(id));
    }
}
