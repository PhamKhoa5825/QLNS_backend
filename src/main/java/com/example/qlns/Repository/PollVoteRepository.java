package com.example.qlns.Repository;

import com.example.qlns.Entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// [Chat] Bình chọn - Repository quản lý lượt vote
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    // [Chat] Lấy tất cả votes của 1 poll
    List<PollVote> findByPollId(Long pollId);

    // [Chat] Lấy votes của user trong 1 poll
    List<PollVote> findByPollIdAndUserId(Long pollId, Long userId);

    // [Chat] Kiểm tra user đã vote option này chưa
    boolean existsByPollIdAndUserIdAndOptionIndex(Long pollId, Long userId, Integer optionIndex);

    // [Chat] Đếm số vote cho từng option
    long countByPollIdAndOptionIndex(Long pollId, Integer optionIndex);
}
