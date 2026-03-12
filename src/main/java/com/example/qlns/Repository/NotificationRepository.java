package com.example.qlns.Repository;

import com.example.qlns.Entity.Notification;
import com.example.qlns.Enum.NotificationTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// =============================================
// TV4 - NotificationRepository
// =============================================
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetType(NotificationTarget targetType);

    List<Notification> findByDepartmentId(Long departmentId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"department", "createdBy"})
    @Query("SELECT n FROM Notification n LEFT JOIN n.department d " +
           "WHERE n.targetType = com.example.qlns.Enum.NotificationTarget.COMPANY OR (n.targetType = com.example.qlns.Enum.NotificationTarget.DEPARTMENT AND d.id = :deptId)")
    List<Notification> findForEmployee(@Param("deptId") Long departmentId);
}
