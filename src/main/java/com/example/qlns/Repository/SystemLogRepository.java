package com.example.qlns.Repository;

import com.example.qlns.Entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// =============================================
// TV2 - SystemLogRepository
// =============================================
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findByUserId(@Param("userId") Long userId);

    List<SystemLog> findByAction(@Param("action") String action);

    List<SystemLog> findTop50ByOrderByCreatedAtDesc();
}
