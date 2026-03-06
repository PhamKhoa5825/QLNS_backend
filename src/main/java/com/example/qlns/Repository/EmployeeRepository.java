package com.example.qlns.Repository;

import com.example.qlns.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Tìm theo user (TV2 dùng sau khi login)
    Optional<Employee> findByUserId(Long userId);

    // Tìm theo mã nhân viên
    Optional<Employee> findByEmployeeCode(String employeeCode);

    // Danh sách theo phòng ban
    List<Employee> findByDepartmentId(Long departmentId);

    // Tìm kiếm theo tên
    List<Employee> findByFullNameContainingIgnoreCase(String keyword);

    // Nhân viên còn đang làm (endDate = null)
    List<Employee> findByEndDateIsNull();

    // Nhân viên theo phòng ban còn làm việc
    List<Employee> findByDepartmentIdAndEndDateIsNull(Long departmentId);

    // Kiểm tra mã NV đã tồn tại chưa
    boolean existsByEmployeeCode(String employeeCode);

    // Kiểm tra CCCD đã tồn tại chưa
    boolean existsByNationalId(String nationalId);
}
