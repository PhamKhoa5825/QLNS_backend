package com.example.qlns.Repository;

import com.example.qlns.Entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// =============================================
// TV4 - UserNotificationRepository
// =============================================
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserId(Long userId);

    List<UserNotification> findByUserIdAndIsRead(Long userId, boolean isRead);

    List<UserNotification> findByUserIdAndNotificationId(Long userId, Long notificationId);

    long countByUserIdAndIsRead(Long userId, boolean isRead);
}
