package com.example.qlns.Service;

import com.example.qlns.Entity.*;
import com.example.qlns.Exception.*;
import com.example.qlns.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// =============================================
// 5. MESSAGE SERVICE (TV4 viết)
// =============================================
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final EmployeeRepository employeeRepository;

    public MessageService(MessageRepository messageRepository,
                          EmployeeRepository employeeRepository) {
        this.messageRepository = messageRepository;
        this.employeeRepository = employeeRepository;
    }

    public Message send(Long senderId, Long receiverId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Nội dung tin nhắn không được để trống");
        }
        Employee sender = employeeRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", senderId));
        Employee receiver = employeeRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên", receiverId));
        return messageRepository.save(new Message(sender, receiver, content));
    }

    public List<Message> getConversation(Long emp1, Long emp2) {
        return messageRepository.findConversation(emp1, emp2);
    }

    // Polling: Android gọi mỗi 3-5 giây
    public List<Message> getNewMessages(Long emp1, Long emp2, Long lastMessageId) {
        return messageRepository.findNewMessages(emp1, emp2, lastMessageId);
    }

    public long getUnreadCount(Long employeeId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(employeeId);
    }

    @Transactional
    public void markAsRead(Long receiverId, Long senderId) {
        messageRepository.markAsRead(receiverId, senderId);
    }
}
