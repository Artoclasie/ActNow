package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizerModel {
    private String organizerId;
    private String userId;
    private String name;
    private String description;
    private String type; // individual, legal_entity
    private Map<String, String> contactInfo;
    private List<String> searchKeywords;
    private Map<String, Object> stats;
    private boolean isVerified;
    private boolean isBanned;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public OrganizerModel() {
        this.contactInfo = new HashMap<>();
        this.searchKeywords = new ArrayList<>();
        this.stats = new HashMap<>();
        this.isVerified = false;
        this.isBanned = false;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public OrganizerModel(String organizerId, String userId, String name, String type) {
        this();
        this.organizerId = organizerId;
        this.userId = userId;
        this.name = name;
        this.type = type;
    }

    // Getters and Setters
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, String> getContactInfo() { return contactInfo; }
    public void setContactInfo(Map<String, String> contactInfo) { this.contactInfo = contactInfo; }

    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isBanned() { return isBanned; }
    public void setBanned(boolean banned) { isBanned = banned; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Contact info getters and setters
    public String getEmail() { return contactInfo.get("email"); }
    public void setEmail(String email) { contactInfo.put("email", email); }

    public String getPhone() { return contactInfo.get("phone"); }
    public void setPhone(String phone) { contactInfo.put("phone", phone); }

    public String getWebsite() { return contactInfo.get("website"); }
    public void setWebsite(String website) { contactInfo.put("website", website); }

    // Stats getters and setters
    public int getEventsCount() { return (int) stats.getOrDefault("eventsCount", 0); }
    public void setEventsCount(int eventsCount) { stats.put("eventsCount", eventsCount); }

    public int getParticipantsCount() { return (int) stats.getOrDefault("participantsCount", 0); }
    public void setParticipantsCount(int participantsCount) { stats.put("participantsCount", participantsCount); }

    public double getRating() { return (double) stats.getOrDefault("rating", 0.0); }
    public void setRating(double rating) { stats.put("rating", rating); }

    // Helper methods
    public void addSearchKeyword(String keyword) {
        if (!searchKeywords.contains(keyword)) {
            searchKeywords.add(keyword);
        }
    }

    public void removeSearchKeyword(String keyword) {
        searchKeywords.remove(keyword);
    }

    public void addContactInfo(String key, String value) {
        contactInfo.put(key, value);
    }

    public void removeContactInfo(String key) {
        contactInfo.remove(key);
    }

    public void updateStat(String key, Object value) {
        stats.put(key, value);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("organizerId", organizerId);
        map.put("userId", userId);
        map.put("name", name);
        map.put("description", description);
        map.put("type", type);
        map.put("contactInfo", contactInfo);
        map.put("searchKeywords", searchKeywords);
        map.put("stats", stats);
        map.put("isVerified", isVerified);
        map.put("isBanned", isBanned);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
