package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateTaskRequest;
import com.example.qlns.DTO.Request.UpdateTaskRequest;
import com.example.qlns.DTO.Request.UpdateTaskStatusRequest;
import com.example.qlns.DTO.Response.TaskDTO;
import com.example.qlns.Entity.Task;
import com.example.qlns.Entity.TaskUpdate;
import com.example.qlns.Enum.TaskPriority;
import com.example.qlns.Enum.TaskStatus;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Security.SecurityService;
import com.example.qlns.Service.EmployeeService;
import com.example.qlns.Service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final EmployeeService empService;
    private final SecurityService securityService;

    TaskController(TaskService taskService, EmployeeService empService, SecurityService securityService) {
        this.taskService = taskService;
        this.empService = empService;
        this.securityService = securityService;
    }

    // GET /api/tasks — Admin xem tất cả tasks
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAll() {
        return ResponseEntity.ok(taskService.getAll());
    }

    // GET /api/tasks/my/{empId}
    @GetMapping("/my/{empId}")
    public ResponseEntity<List<TaskDTO>> getMyTasks(@PathVariable Long empId) {
        return ResponseEntity.ok(taskService.getMyTasks(empId));
    }

    // GET /api/tasks/department/{deptId}
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<TaskDTO>> getByDepartment(@PathVariable Long deptId) {
        // Manager chỉ xem được task phòng ban mình, Admin xem tất cả
        securityService.validateManagerDepartment(deptId);
        return ResponseEntity.ok(taskService.getByDepartment(deptId));
    }

    // GET /api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    // POST /api/tasks
    @PostMapping
    public ResponseEntity<TaskDTO> create(@RequestBody CreateTaskRequest req) {
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setPriority(TaskPriority.valueOf(req.getPriority()));
        if (req.getDeadline() != null)
            task.setDeadline(LocalDate.parse(req.getDeadline()).atStartOfDay());
        task.setAttachmentUrl(req.getAttachmentUrl());

        task.setAssignedTo(empService.getById(req.getAssignedToId()));
        task.setAssignedBy(empService.getById(req.getAssignedById()));

        return ResponseEntity.ok(taskService.create(task));
    }

    // PUT /api/tasks/{id} — Chỉnh sửa nhiệm vụ (Manager/Admin)
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id,
                                          @RequestBody UpdateTaskRequest req) {
        return ResponseEntity.ok(taskService.updateTask(id, req));
    }

    // PUT /api/tasks/{id}/accept
    @PutMapping("/{id}/accept")
    public ResponseEntity<TaskDTO> acceptTask(@PathVariable Long id,
                                              @RequestBody Map<String, Long> body) {
        Long employeeId = body.get("employeeId");
        if (employeeId == null)
            throw new ResourceNotFoundException("Thiếu employeeId trong body");
        return ResponseEntity.ok(taskService.acceptTask(id, employeeId));
    }

    // PUT /api/tasks/{id}/status?updatedById=3
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateStatus(@PathVariable Long id,
                                                @RequestBody UpdateTaskStatusRequest req,
                                                @RequestParam Long updatedById) {
        return ResponseEntity.ok(taskService.updateStatus(
                id, TaskStatus.valueOf(req.getStatus()), req.getNote(), updatedById));
    }

    // DELETE /api/tasks/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/tasks/{id}/history
    @GetMapping("/{id}/history")
    public ResponseEntity<List<TaskUpdate>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getHistory(id));
    }
}