package com.example.qlns.Repository;

import com.example.qlns.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// [Chat] Repository quản lý tin nhắn
public interface MessageRepository extends JpaRepository<Message, Long> {

    // [Chat] Lấy tin nhắn theo phòng, sắp xếp theo thời gian
    List<Message> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // [Chat] Polling - lấy tin nhắn mới hơn lastId
    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.id > :lastId ORDER BY m.createdAt ASC")
    List<Message> findNewMessages(@Param("roomId") Long roomId,
                                  @Param("lastId") Long lastMessageId);

    // [Chat] Lấy tin nhắn mới hơn ID nhất định
    List<Message> findByRoomIdAndIdGreaterThan(Long roomId, Long lastId);

    // [Chat] Đếm tin nhắn chưa đọc (id > lastReadMessageId)
    long countByRoomIdAndIdGreaterThan(Long roomId, Long lastReadMessageId);
}
