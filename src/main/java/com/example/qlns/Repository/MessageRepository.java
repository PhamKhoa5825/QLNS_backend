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



    // [Chat] Đếm tin nhắn chưa đọc (id > lastReadMessageId)
    long countByRoomIdAndIdGreaterThan(Long roomId, Long lastReadMessageId);

    // [Chat] Tìm kiếm tin nhắn theo keyword trong phòng chat (loại trừ tin đã thu hồi)
    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.isRecalled = false " +
           "AND LOWER(m.message) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.createdAt ASC")
    List<Message> searchMessages(@Param("roomId") Long roomId, @Param("keyword") String keyword);
}

