package com.example.qlns.Repository;

import com.example.qlns.Entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// [Chat] Repository quản lý thành viên phòng chat - mở rộng tìm/xóa theo user
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // [Chat] Lấy danh sách phòng chat của user
    List<ChatRoomMember> findByUserId(Long userId);

    // [Chat] Lấy danh sách thành viên của phòng
    List<ChatRoomMember> findByRoomId(Long roomId);

    // [Chat] Kiểm tra user có trong phòng không
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    // [Chat] Đồng bộ nhân sự - Tìm member cụ thể để cập nhật/xóa
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    // [Chat] Đồng bộ nhân sự - Xóa member khỏi phòng khi nghỉ việc
    void deleteByRoomIdAndUserId(Long roomId, Long userId);
}
