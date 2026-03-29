package com.example.qlns.Repository;

import com.example.qlns.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Lấy tin nhắn theo phòng, cũ nhất trước
    List<Message> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // Đếm số tin nhắn sau lastReadMessageId
    long countByRoomIdAndIdGreaterThan(Long roomId, Long lastReadMessageId);

    // Tìm kiếm theo keyword trong phòng, bỏ qua tin đã thu hồi
    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.isRecalled = false " +
            "AND LOWER(m.message) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.createdAt ASC")
    List<Message> searchMessages(@Param("roomId") Long roomId, @Param("keyword") String keyword);
}
