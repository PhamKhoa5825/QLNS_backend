package com.example.qlns.Repository;

import com.example.qlns.Entity.Request;
import com.example.qlns.Enum.RequestStatus;
import com.example.qlns.Enum.TargetRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    // Tất cả đơn của một nhân viên (mới nhất trước)
    List<Request> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // Lọc theo trạng thái
    List<Request> findByEmployeeIdAndStatusOrderByCreatedAtDesc(
            Long employeeId, RequestStatus status);

    // Đơn chờ duyệt của phòng ban (Manager xem)
    @Query("""
        SELECT r FROM Request r
        WHERE r.employee.department.id = :deptId
        ORDER BY r.createdAt DESC
    """)
    List<Request> findByDepartmentId(@Param("deptId") Long deptId);

    // Đơn chờ duyệt của phòng ban lọc theo status
    @Query("""
        SELECT r FROM Request r
        WHERE r.employee.department.id = :deptId
          AND r.status = :status
        ORDER BY r.createdAt DESC
    """)
    List<Request> findByDepartmentIdAndStatus(
            @Param("deptId") Long deptId,
            @Param("status") RequestStatus status);

    // Tất cả đơn toàn công ty (Admin xem)
    List<Request> findAllByOrderByCreatedAtDesc();

    // Đơn chờ duyệt toàn công ty
    List<Request> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    long countByStatus(RequestStatus status);

    // Đơn gửi cho Admin (Admin chỉ thấy đơn gửi cho mình)
    List<Request> findByTargetRoleOrderByCreatedAtDesc(TargetRole targetRole);

    // Đơn gửi cho Manager theo phòng ban (Manager chỉ thấy đơn gửi cho Manager)
    @Query("""
        SELECT r FROM Request r
        WHERE r.employee.department.id = :deptId
          AND r.targetRole = 'MANAGER'
        ORDER BY r.createdAt DESC
    """)
    List<Request> findByDepartmentIdAndTargetRole(@Param("deptId") Long deptId);

    // Đơn pending gửi cho Manager theo phòng ban
    @Query("""
        SELECT r FROM Request r
        WHERE r.employee.department.id = :deptId
          AND r.status = :status
          AND r.targetRole = 'MANAGER'
        ORDER BY r.createdAt DESC
    """)
    List<Request> findByDepartmentIdAndStatusAndTargetRoleManager(
            @Param("deptId") Long deptId,
            @Param("status") RequestStatus status);
}
