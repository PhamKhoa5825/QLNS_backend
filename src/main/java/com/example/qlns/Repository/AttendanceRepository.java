package com.example.qlns.Repository;

import com.example.qlns.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// =============================================
// TV3 - AttendanceRepository
// =============================================
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<Attendance> findByEmployeeId(Long employeeId);
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);
    List<Attendance> findByDate(LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :empId " +
            "AND MONTH(a.date) = :month AND YEAR(a.date) = :year " +
            "AND a.status != 'ABSENT'")
    long countWorkingDays(@Param("empId") Long empId,
                          @Param("month") int month,
                          @Param("year") int year);

    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
}

