package com.example.qlns.Repository;

import com.example.qlns.Entity.LeaveRequest;
import com.example.qlns.Enum.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// =============================================
// TV3 - LeaveRequestRepository
// =============================================
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.department.id = :deptId")
    List<LeaveRequest> findByDepartmentId(@Param("deptId") Long departmentId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.department.id = :deptId " +
            "AND lr.status = :status")
    List<LeaveRequest> findByDepartmentIdAndStatus(@Param("deptId") Long departmentId,
                                                   @Param("status") LeaveStatus status);
}
