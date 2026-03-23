package com.example.qlns.Service;

import com.example.qlns.DTO.Response.PollDTO;
import com.example.qlns.Entity.*;
import com.example.qlns.Enum.MessageType;
import com.example.qlns.Exception.ForbiddenException;
import com.example.qlns.Exception.ResourceNotFoundException;
import com.example.qlns.Repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

// [Chat] Bình chọn - Service quản lý Poll (tạo, vote, đóng, xem kết quả)
// Tuân thủ Single Responsibility: chỉ xử lý logic bình chọn
@Service
public class PollService {

    private final PollRepository pollRepo;
    private final PollVoteRepository voteRepo;
    private final ChatRoomRepository roomRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PollService(PollRepository pollRepo,
                       PollVoteRepository voteRepo,
                       ChatRoomRepository roomRepo,
                       UserRepository userRepo,
                       MessageRepository messageRepo) {
        this.pollRepo = pollRepo;
        this.voteRepo = voteRepo;
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

    // [Chat] Bình chọn - Tạo poll mới và gửi vào phòng chat
    @Transactional
    public PollDTO createPoll(Long roomId, Long creatorId, String question,
                               List<String> options, java.time.LocalDateTime deadline) {
        ChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng chat"));
        User creator = userRepo.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người tạo"));

        // [Chat] Chuyển options list thành JSON string để lưu DB
        String optionsJson;
        try {
            optionsJson = objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("[Chat] Lỗi chuyển đổi options sang JSON", e);
        }

        // [Chat] Tạo tin nhắn type=POLL trong phòng chat
        Message pollMessage = new Message();
        pollMessage.setRoom(room);
        pollMessage.setSender(creator);
        pollMessage.setMessage("📊 " + question);
        pollMessage.setMessageType(MessageType.POLL);
        Message savedMsg = messageRepo.save(pollMessage);

        // [Chat] Tạo entity Poll
        Poll poll = new Poll();
        poll.setRoom(room);
        poll.setCreator(creator);
        poll.setQuestion(question);
        poll.setOptions(optionsJson);
        poll.setMessageId(savedMsg.getId());
        poll.setDeadline(deadline);
        Poll saved = pollRepo.save(poll);

        PollDTO dto = buildPollDTO(saved, creatorId);
        return dto;
    }

    // [Chat] Bình chọn - Vote cho 1 option
    @Transactional
    public PollDTO vote(Long pollId, Long userId, Integer optionIndex) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy poll"));

        if (poll.getIsClosed()) {
            throw new ForbiddenException("Poll đã đóng, không thể vote");
        }

        // [Chat] Poll chỉ cho 1 đáp án → xóa vote cũ trước khi vote mới
        List<PollVote> existingVotes = voteRepo.findByPollIdAndUserId(pollId, userId);
        voteRepo.deleteAll(existingVotes);

        // [Chat] Kiểm tra đã vote option này chưa (toggle)
        if (voteRepo.existsByPollIdAndUserIdAndOptionIndex(pollId, userId, optionIndex)) {
            // [Chat] Đã vote rồi → bỏ vote
            List<PollVote> votes = voteRepo.findByPollIdAndUserId(pollId, userId);
            votes.stream()
                    .filter(v -> v.getOptionIndex().equals(optionIndex))
                    .forEach(voteRepo::delete);
        } else {
            // [Chat] Chưa vote → thêm vote
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
            PollVote vote = new PollVote();
            vote.setPoll(poll);
            vote.setUser(user);
            vote.setOptionIndex(optionIndex);
            voteRepo.save(vote);
        }

        return buildPollDTO(poll, userId);
    }

    // [Chat] Bình chọn - Lấy kết quả poll
    public PollDTO getPollResult(Long pollId, Long currentUserId) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy poll"));
        return buildPollDTO(poll, currentUserId);
    }

    // [Chat] Bình chọn - Đóng poll (chỉ người tạo)
    @Transactional
    public PollDTO closePoll(Long pollId, Long userId) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy poll"));

        if (!poll.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("Chỉ người tạo mới có thể đóng poll");
        }

        poll.setIsClosed(true);
        pollRepo.save(poll);
        return buildPollDTO(poll, userId);
    }

    // [Chat] Bình chọn - Helper tạo PollDTO với vote counts và myVotes
    private PollDTO buildPollDTO(Poll poll, Long currentUserId) {
        PollDTO dto = PollDTO.from(poll);

        // [Chat] Parse options từ JSON
        try {
            List<String> optionsList = objectMapper.readValue(poll.getOptions(), new TypeReference<>() {});
            dto.setOptions(optionsList);
        } catch (JsonProcessingException e) {
            dto.setOptions(List.of());
        }

        // [Chat] Đếm số vote từng option
        List<PollVote> allVotes = voteRepo.findByPollId(poll.getId());
        Map<Integer, Long> voteCounts = allVotes.stream()
                .collect(Collectors.groupingBy(PollVote::getOptionIndex, Collectors.counting()));
        dto.setVoteCounts(voteCounts);

        // [Chat] Tổng số người đã vote (unique users)
        int totalVoters = (int) allVotes.stream()
                .map(v -> v.getUser().getId()).distinct().count();
        dto.setTotalVoters(totalVoters);

        // [Chat] Các option user hiện tại đã chọn
        if (currentUserId != null) {
            List<Integer> myVotes = voteRepo.findByPollIdAndUserId(poll.getId(), currentUserId)
                    .stream().map(PollVote::getOptionIndex).toList();
            dto.setMyVotes(myVotes);
        }

        return dto;
    }
}
