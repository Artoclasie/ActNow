package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class LikeModel {
    private String likeId;
    private String userId;
    private String targetType; // post, comment, event
    private String targetId;
    private Timestamp createdAt;

    public LikeModel() {
        this.createdAt = Timestamp.now();
    }

    public LikeModel(String likeId, String userId, String targetType, String targetId) {
        this();
        this.likeId = likeId;
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    // Getters and Setters
    public String getLikeId() { return likeId; }
    public void setLikeId(String likeId) { this.likeId = likeId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("likeId", likeId);
        map.put("userId", userId);
        map.put("targetType", targetType);
        map.put("targetId", targetId);
        map.put("createdAt", createdAt);
        return map;
    }
} 