package com.example.qlns.Repository;

import com.example.qlns.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssigneeIdOrderByCreatedAtDesc(Long assigneeId);

    List<Task> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    List<Task> findByAssigneeIdAndStatus(Long assigneeId, Task.TaskStatus status);

    List<Task> findByDepartmentId(Long departmentId);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :empId " +
            "AND t.status != 'DONE' AND t.deadline <= :deadline")
    List<Task> findUpcomingDeadlines(@Param("empId") Long empId,
                                     @Param("deadline") java.time.LocalDateTime deadline);
}
