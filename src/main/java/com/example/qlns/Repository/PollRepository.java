package com.example.qlns.Repository;

import com.example.qlns.Entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// [Chat] Bình chọn - Repository quản lý polls
public interface PollRepository extends JpaRepository<Poll, Long> {

    // [Chat] Lấy tất cả polls trong phòng chat
    List<Poll> findByRoomId(Long roomId);

    // [Chat] Lấy poll theo messageId
    Poll findByMessageId(Long messageId);
}
