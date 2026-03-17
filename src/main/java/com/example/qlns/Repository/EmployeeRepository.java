package com.example.qlns.Repository;

import com.example.qlns.Entity.Employee;
import com.example.qlns.Enum.EmployeeStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByStatus(EmployeeStatus status);

    @EntityGraph(attributePaths = {"department"})
    List<Employee> findByDepartmentIdAndStatus(Long departmentId, EmployeeStatus status);

    @EntityGraph(attributePaths = {"department"})
    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE' AND " +
            "(LOWER(e.fullName) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
            "LOWER(e.position) LIKE LOWER(CONCAT('%',:kw,'%')))")
    List<Employee> search(@Param("kw") String keyword);

    @EntityGraph(attributePaths = {"department"})
    @Query("SELECT e FROM Employee e")
    List<Employee> findAllWithDept();

    long countByDepartmentIdAndStatus(Long departmentId, EmployeeStatus status);

    long countByStatus(EmployeeStatus status);

    @Query("SELECT e.department.id FROM Employee e WHERE e.id = :empId")
    Long findDepartmentIdByEmployeeId(@Param("empId") Long empId);
}
