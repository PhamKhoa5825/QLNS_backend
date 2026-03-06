package com.example.qlns.Repository;

import com.example.qlns.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);

    List<Attendance> findByDate(LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);

    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // Kiểm tra hôm nay có bản ghi LEAVE không
    boolean existsByEmployeeIdAndDateAndStatus(
            Long employeeId,
            LocalDate date,
            Attendance.AttendanceStatus status
    );

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :empId " +
            "AND a.date BETWEEN :from AND :to AND a.status = 'PRESENT'")
    long countPresentDays(@Param("empId") Long empId,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to);
}