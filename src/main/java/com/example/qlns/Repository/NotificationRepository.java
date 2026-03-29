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

    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN UserNotification un ON un.notification.id = n.id " +
            "WHERE n.targetType = com.example.qlns.Enum.NotificationTarget.COMPANY " +
            "OR (n.department.id = :deptId AND n.targetType = com.example.qlns.Enum.NotificationTarget.DEPARTMENT) " +
            "OR (un.user.id = :userId AND n.targetType = com.example.qlns.Enum.NotificationTarget.SPECIFIC_USERS) " +
            "ORDER BY n.id DESC")
    List<Notification> findForUser(@Param("deptId") Long departmentId, @Param("userId") Long userId);
}
