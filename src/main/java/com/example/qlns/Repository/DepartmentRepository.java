package com.example.qlns.Repository;

import com.example.qlns.Entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// =============================================
// TV1 - DepartmentRepository
// =============================================
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByName(String name);
    List<Department> findByManagerId(Long managerId);
    Optional<Department> findByName(String name);
}
