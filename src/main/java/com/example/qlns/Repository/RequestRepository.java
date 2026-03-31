package com.example.qlns.Repository;

import com.example.qlns.Entity.Request;
import com.example.qlns.Enum.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("SELECT DISTINCT r FROM Request r LEFT JOIN FETCH r.details WHERE r.employee.id = :employeeId AND r.status = :status")
    List<Request> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") RequestStatus status);
    
    @Query("SELECT DISTINCT r FROM Request r LEFT JOIN FETCH r.employee e LEFT JOIN FETCH e.department " +
           "WHERE e.id = :employeeId " +
           "AND (:month IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('MONTH', rd.specificDate) = :month)) " +
           "AND (:year IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('YEAR', rd.specificDate) = :year)) " +
           "ORDER BY r.createdAt DESC")
    List<Request> findByEmployeeIdFiltered(@Param("employeeId") Long employeeId, @Param("month") Integer month, @Param("year") Integer year);
    
    List<Request> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    @Query("SELECT r FROM Request r WHERE r.status = 'PENDING' AND NOT EXISTS (" +
           "SELECT d FROM RequestDetail d WHERE d.request = r AND d.specificDate >= :today)")
    List<Request> findExpiredPendingRequests(@Param("today") LocalDate today);
    
    @Query("SELECT r FROM Request r WHERE r.employee.department.id = :deptId")
    List<Request> findByDepartmentId(@Param("deptId") Long deptId);
    
    @Query("SELECT r FROM Request r WHERE r.employee.department.id = :deptId AND r.status = :status")
    List<Request> findByDepartmentIdAndStatus(@Param("deptId") Long deptId, @Param("status") RequestStatus status);

    @Query("SELECT r FROM Request r JOIN User u ON r.employee.id = u.employeeId " +
           "WHERE r.employee.department.id = :deptId AND r.status = 'PENDING' AND u.role = 'EMPLOYEE' " +
           "AND r.type != com.example.qlns.Enum.RequestType.RESIGNATION " + // Thêm dòng này để giấu đơn thôi việc khỏi Quản lý
           "AND (:empId IS NULL OR r.employee.id = :empId) " +
           "AND (:month IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('MONTH', rd.specificDate) = :month)) " +
           "AND (:year IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('YEAR', rd.specificDate) = :year)) " +
           "ORDER BY r.createdAt DESC")
    List<Request> findEmployeePendingRequests(
            @Param("deptId") Long deptId,
            @Param("empId") Long empId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    @Query("SELECT DISTINCT r FROM Request r JOIN User u ON r.employee.id = u.employeeId " +
           "WHERE r.status = 'PENDING' AND (u.role = 'MANAGER' OR u.role = 'EMPLOYEE') " +
           "AND (:deptId IS NULL OR r.employee.department.id = :deptId) " +
           "AND (:empId IS NULL OR r.employee.id = :empId) " +
           "AND (:month IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('MONTH', rd.specificDate) = :month)) " +
           "AND (:year IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('YEAR', rd.specificDate) = :year)) " +
           "ORDER BY r.createdAt DESC")
    List<Request> findManagerPendingRequests(
            @Param("deptId") Long deptId,
            @Param("empId") Long empId,
            @Param("month") Integer month,
            @Param("year") Integer year);

    List<Request> findAllByOrderByCreatedAtDesc();
    
    List<Request> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    @Query("SELECT DISTINCT r FROM Request r " +
           "LEFT JOIN FETCH r.employee e " +
           "LEFT JOIN FETCH e.department " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:month IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('MONTH', rd.specificDate) = :month)) " +
           "AND (:year IS NULL OR EXISTS (SELECT rd FROM r.details rd WHERE FUNCTION('YEAR', rd.specificDate) = :year)) " +
           "AND (:deptId IS NULL OR e.department.id = :deptId) " +
           "AND (:empId IS NULL OR e.id = :empId) " +
           "ORDER BY r.createdAt DESC")
    List<Request> findFilteredRequests(
            @Param("status") RequestStatus status,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("deptId") Long deptId,
            @Param("empId") Long empId);

    long countByStatus(RequestStatus status);
}
