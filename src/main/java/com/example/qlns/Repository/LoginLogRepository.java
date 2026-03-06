package com.example.qlns.Repository;

import com.example.qlns.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByUserIdOrderByLoginAtDesc(Long userId);

    Optional<LoginLog> findTopByUserIdOrderByLoginAtDesc(Long userId);

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.user.id = :userId " +
            "AND l.isSuccess = false AND l.loginAt >= :since")
    long countFailedAttempts(@Param("userId") Long userId,
                             @Param("since") java.time.LocalDateTime since);
}
