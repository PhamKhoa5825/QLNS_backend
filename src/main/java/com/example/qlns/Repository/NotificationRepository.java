package com.example.qlns.Repository;

import com.example.qlns.Entity.Notification;
import com.example.qlns.Enum.NotificationTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetType(NotificationTarget targetType);
    List<Notification> findByDepartmentId(Long departmentId);

    /**
     * Lấy thông báo cho 1 nhân viên:
     * - Toàn công ty (COMPANY)
     * - Phòng ban của họ (DEPARTMENT + deptId)
     * - Gửi riêng cho họ (EMPLOYEE + targetEmployeeId)
     */
    @Query("SELECT n FROM Notification n WHERE n.targetType = 'COMPANY' " +
            "OR (n.targetType = 'DEPARTMENT' AND n.department.id = :deptId) " +
            "OR (n.targetType = 'EMPLOYEE' AND n.targetEmployeeId = :empId) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findForEmployee(@Param("deptId") Long departmentId,
                                       @Param("empId") Long employeeId);

    // Giữ method cũ cho tương thích (nếu cần)
    @Query("SELECT n FROM Notification n WHERE n.targetType = 'COMPANY' " +
            "OR (n.targetType = 'DEPARTMENT' AND n.department.id = :deptId) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findForDepartment(@Param("deptId") Long departmentId);
}