package com.example.qlns.Repository;

import com.example.qlns.Entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    Optional<SalaryRecord> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);

    List<SalaryRecord> findByEmployeeId(Long employeeId);

    @Query("SELECT s FROM SalaryRecord s WHERE s.employee.department.id = :deptId AND s.month = :month AND s.year = :year")
    List<SalaryRecord> findByDepartmentAndMonthAndYear(@Param("deptId") Long deptId, @Param("month") int month, @Param("year") int year);

    List<SalaryRecord> findByMonthAndYear(int month, int year);
}
