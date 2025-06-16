package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ChatModel {
    private String chatId;
    private List<String> participants;
    private String lastMessage;
    private Timestamp lastMessageTime;
    private Map<String, Integer> unreadCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ChatModel() {
        this.participants = new ArrayList<>(2);
        this.unreadCount = new HashMap<>();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public ChatModel(String chatId, String user1, String user2) {
        this();
        this.chatId = chatId;
        this.participants.add(user1);
        this.participants.add(user2);
    }

    // Getters and Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) {
        if (participants != null && participants.size() == 2) {
            this.participants = new ArrayList<>(participants);
        } else {
            throw new IllegalArgumentException("Participants must be exactly 2 for 1:1 chat");
        }
    }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Timestamp getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Timestamp lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public Map<String, Integer> getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Map<String, Integer> unreadCount) { this.unreadCount = unreadCount; }
    public void incrementUnreadCount(String userId) { unreadCount.put(userId, unreadCount.getOrDefault(userId, 0) + 1); }
    public void resetUnreadCount(String userId) { unreadCount.put(userId, 0); }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("chatId", chatId);
        map.put("participants", participants);
        map.put("lastMessage", lastMessage);
        map.put("lastMessageTime", lastMessageTime);
        map.put("unreadCount", unreadCount);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}