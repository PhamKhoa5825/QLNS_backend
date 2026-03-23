package com.example.qlns.Repository;

import com.example.qlns.Entity.MessageReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// [Chat] Đã xem - Repository quản lý read receipts của tin nhắn
public interface MessageReadReceiptRepository extends JpaRepository<MessageReadReceipt, Long> {

    // [Chat] Lấy danh sách người đã xem tin nhắn
    List<MessageReadReceipt> findByMessageId(Long messageId);

    // [Chat] Kiểm tra user đã xem tin nhắn này chưa
    Optional<MessageReadReceipt> findByMessageIdAndUserId(Long messageId, Long userId);

    // [Chat] Kiểm tra tồn tại
    boolean existsByMessageIdAndUserId(Long messageId, Long userId);
}
