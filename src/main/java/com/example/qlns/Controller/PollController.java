package com.example.qlns.Controller;

import com.example.qlns.DTO.Request.CreatePollRequest;
import com.example.qlns.DTO.Request.PollVoteRequest;
import com.example.qlns.DTO.Response.PollDTO;
import com.example.qlns.Service.PollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// [Chat] Bình chọn (Poll) - REST Controller quản lý vote nhanh trong phòng chat
// Tuân thủ MVC: Controller chỉ nhận request, gọi PollService, trả response
@RestController
@RequestMapping("/api/chat/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    // [Chat] Bình chọn - Tạo poll mới
    @PostMapping
    public ResponseEntity<PollDTO> createPoll(@RequestBody CreatePollRequest request,
                                               @RequestParam Long creatorId) {
        PollDTO dto = pollService.createPoll(
                request.getRoomId(), creatorId, request.getQuestion(),
                request.getOptions(), request.getDeadline()
        );
        return ResponseEntity.ok(dto);
    }

    // [Chat] Bình chọn - Vote cho 1 option
    @PostMapping("/{pollId}/vote")
    public ResponseEntity<PollDTO> vote(@PathVariable Long pollId,
                                         @RequestBody PollVoteRequest request,
                                         @RequestParam Long userId) {
        PollDTO dto = pollService.vote(pollId, userId, request.getOptionIndex());
        return ResponseEntity.ok(dto);
    }

    // [Chat] Bình chọn - Lấy kết quả poll
    @GetMapping("/{pollId}")
    public ResponseEntity<PollDTO> getPollResult(@PathVariable Long pollId,
                                                   @RequestParam Long userId) {
        PollDTO dto = pollService.getPollResult(pollId, userId);
        return ResponseEntity.ok(dto);
    }

    // [Chat] Bình chọn - Đóng poll (chỉ người tạo)
    @PutMapping("/{pollId}/close")
    public ResponseEntity<PollDTO> closePoll(@PathVariable Long pollId,
                                               @RequestParam Long userId) {
        PollDTO dto = pollService.closePoll(pollId, userId);
        return ResponseEntity.ok(dto);
    }
}
