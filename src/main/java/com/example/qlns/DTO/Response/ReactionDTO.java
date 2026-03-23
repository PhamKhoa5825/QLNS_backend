package com.example.qlns.DTO.Response;

import java.util.List;

// [Chat] Reaction - DTO tổng hợp reactions theo emoji cho 1 tin nhắn
public class ReactionDTO {

    // [Chat] Mã emoji
    private String emoji;

    // [Chat] Tổng số người react emoji này
    private int count;

    // [Chat] Danh sách userId đã react
    private List<Long> userIds;

    // [Chat] Danh sách tên user đã react
    private List<String> userNames;

    public ReactionDTO() {
    }

    public ReactionDTO(String emoji, int count, List<Long> userIds, List<String> userNames) {
        this.emoji = emoji;
        this.count = count;
        this.userIds = userIds;
        this.userNames = userNames;
    }

    // ── Getters ──────────────────────────────────────

    public String getEmoji() {
        return emoji;
    }

    public int getCount() {
        return count;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public List<String> getUserNames() {
        return userNames;
    }
}
