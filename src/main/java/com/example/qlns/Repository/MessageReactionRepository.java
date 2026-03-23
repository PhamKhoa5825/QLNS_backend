package com.example.qlns.Repository;

import com.example.qlns.Entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// [Chat] Reaction - Repository quản lý cảm xúc trên tin nhắn
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    // [Chat] Lấy tất cả reactions của 1 tin nhắn
    List<MessageReaction> findByMessageId(Long messageId);

    // [Chat] Tìm reaction cụ thể của user trên tin nhắn với emoji nhất định
    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    // [Chat] Xóa reaction của user
    void deleteByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);
}
