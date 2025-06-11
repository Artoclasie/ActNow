package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatModel {
    private String chatId;
    private String eventId;
    private List<String> participants;
    private String lastMessage;
    private Timestamp lastMessageTime;
    private int unreadCount;
    private boolean isGroup;
    private String groupName;
    private String groupImage;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public ChatModel() {
        this.participants = new ArrayList<>();
        this.unreadCount = 0;
        this.isGroup = false;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public ChatModel(String chatId, String eventId, List<String> participants) {
        this();
        this.chatId = chatId;
        this.eventId = eventId;
        this.participants = participants;
        this.isGroup = participants.size() > 2;
    }

    // Getters and Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { 
        this.participants = participants;
        this.isGroup = participants.size() > 2;
    }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Timestamp getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Timestamp lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupImage() { return groupImage; }
    public void setGroupImage(String groupImage) { this.groupImage = groupImage; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void addParticipant(String userId) {
        if (!participants.contains(userId)) {
            participants.add(userId);
            isGroup = participants.size() > 2;
        }
    }

    public void removeParticipant(String userId) {
        participants.remove(userId);
        isGroup = participants.size() > 2;
    }

    public void incrementUnreadCount() {
        unreadCount++;
    }

    public void resetUnreadCount() {
        unreadCount = 0;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("chatId", chatId);
        map.put("eventId", eventId);
        map.put("participants", participants);
        map.put("lastMessage", lastMessage);
        map.put("lastMessageTime", lastMessageTime);
        map.put("unreadCount", unreadCount);
        map.put("isGroup", isGroup);
        map.put("groupName", groupName);
        map.put("groupImage", groupImage);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
