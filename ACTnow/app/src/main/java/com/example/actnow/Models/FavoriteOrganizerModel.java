package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class FavoriteOrganizerModel {
    private String favoriteId;
    private String userId;
    private String organizerId;
    private Timestamp createdAt;

    public FavoriteOrganizerModel() {
        this.createdAt = Timestamp.now();
    }

    public FavoriteOrganizerModel(String favoriteId, String userId, String organizerId) {
        this();
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.organizerId = organizerId;
    }

    // Getters and Setters
    public String getFavoriteId() { return favoriteId; }
    public void setFavoriteId(String favoriteId) { this.favoriteId = favoriteId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("favoriteId", favoriteId);
        map.put("userId", userId);
        map.put("organizerId", organizerId);
        map.put("createdAt", createdAt);
        return map;
    }
} 