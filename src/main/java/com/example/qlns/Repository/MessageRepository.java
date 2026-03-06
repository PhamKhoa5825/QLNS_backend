package com.example.qlns.Repository;

import com.example.qlns.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :emp1 AND m.receiver.id = :emp2) OR " +
            "(m.sender.id = :emp2 AND m.receiver.id = :emp1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("emp1") Long emp1, @Param("emp2") Long emp2);

    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :emp1 AND m.receiver.id = :emp2) OR " +
            "(m.sender.id = :emp2 AND m.receiver.id = :emp1)) " +
            "AND m.id > :lastMessageId ORDER BY m.createdAt ASC")
    List<Message> findNewMessages(@Param("emp1") Long emp1,
                                  @Param("emp2") Long emp2,
                                  @Param("lastMessageId") Long lastMessageId);

    List<Message> findByDepartmentIdOrderByCreatedAtAsc(Long departmentId);

    long countByReceiverIdAndIsReadFalse(Long receiverId);

    @Query("UPDATE Message m SET m.isRead = true " +
            "WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId")
    void markAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}