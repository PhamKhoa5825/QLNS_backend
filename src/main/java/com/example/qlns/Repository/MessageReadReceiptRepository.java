package com.example.qlns.Repository;

import com.example.qlns.Entity.MessageReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReadReceiptRepository extends JpaRepository<MessageReadReceipt, Long> {

    List<MessageReadReceipt> findByMessageId(Long messageId);

    Optional<MessageReadReceipt> findByMessageIdAndUserId(Long messageId, Long userId);

    boolean existsByMessageIdAndUserId(Long messageId, Long userId);
}
