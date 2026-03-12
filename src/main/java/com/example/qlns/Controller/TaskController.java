package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreateTaskRequest;
import com.example.qlns.DTO.Request.UpdateTaskStatusRequest;
import com.example.qlns.DTO.Response.TaskDTO;
import com.example.qlns.Entity.Task;
import com.example.qlns.Entity.TaskUpdate;
import com.example.qlns.Enum.TaskPriority;
import com.example.qlns.Enum.TaskStatus;
import com.example.qlns.Exception.ResourceNotFoundException;
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

    TaskController(TaskService taskService, EmployeeService empService) {
        this.taskService = taskService;
        this.empService = empService;
    }

    // GET /api/tasks/my/{empId}
    @GetMapping("/my/{empId}")
    public ResponseEntity<List<TaskDTO>> getMyTasks(@PathVariable Long empId) {
        // Service đã trả về List<TaskDTO> → bỏ .stream().map(TaskDTO::from)
        return ResponseEntity.ok(taskService.getMyTasks(empId));
    }

    // GET /api/tasks/department/{deptId}
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<TaskDTO>> getByDepartment(@PathVariable Long deptId) {
        // Service đã trả về List<TaskDTO> → bỏ .stream().map(TaskDTO::from)
        return ResponseEntity.ok(taskService.getByDepartment(deptId));
    }

    // GET /api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getById(@PathVariable Long id) {
        // Service đã trả về TaskDTO → bỏ TaskDTO.from(...)
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

        // getById() đã ném ResourceNotFoundException nếu không tìm thấy
        // Bỏ dòng empService.getById(...).equals(null) — sai cú pháp, gọi getById() 2 lần thừa
        task.setAssignedTo(empService.getById(req.getAssignedToId()));
        task.setAssignedBy(empService.getById(req.getAssignedById()));

        // Service đã trả về TaskDTO → bỏ TaskDTO.from(...)
        return ResponseEntity.ok(taskService.create(task));
    }

    // PUT /api/tasks/{id}/accept  ← endpoint mới, TV3 thêm vào
    // Body: { "employeeId": 3 }
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
        // Service đã trả về TaskDTO → bỏ TaskDTO.from(...)
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