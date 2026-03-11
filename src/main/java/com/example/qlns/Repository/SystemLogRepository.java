package com.example.qlns.Repository;

import com.example.qlns.Entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// =============================================
// TV2 - SystemLogRepository
// =============================================
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findByUserId(Long userId);

    List<SystemLog> findByAction(String action);

    List<SystemLog> findTop50ByOrderByCreatedAtDesc();
}
