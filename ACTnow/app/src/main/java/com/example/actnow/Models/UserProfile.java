package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfile {
    private String userId;
    private String email;
    private String passwordHash;
    private String username;
    private String customTag;
    private Map<String, Object> profile;
    private List<String> interests;
    private List<String> searchKeywords;
    private String accountType; // volunteer, individual_organizer, legal_entity
    private boolean isAdmin;
    private boolean isVerified;
    private boolean isBanned;
    private Map<String, Object> stats;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public UserProfile() {
        this.profile = new HashMap<>();
        this.interests = new ArrayList<>();
        this.searchKeywords = new ArrayList<>();
        this.stats = new HashMap<>();
        this.isAdmin = false;
        this.isBanned = false;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public UserProfile(String userId, String email, String username, String accountType) {
        this();
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.accountType = accountType;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCustomTag() { return customTag; }
    public void setCustomTag(String customTag) { 
        this.customTag = customTag != null && !customTag.startsWith("@") ? "@" + customTag : customTag;
    }

    public Map<String, Object> getProfile() { return profile; }
    public void setProfile(Map<String, Object> profile) { this.profile = profile; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isBanned() { return isBanned; }
    public void setBanned(boolean banned) { isBanned = banned; }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Profile getters and setters
    public String getName() { return (String) profile.get("name"); }
    public void setName(String name) { profile.put("name", name); }

    public String getAvatarUrl() { return (String) profile.get("avatarUrl"); }
    public void setAvatarUrl(String avatarUrl) { profile.put("avatarUrl", avatarUrl); }

    public String getBackgroundImageUrl() { return (String) profile.get("backgroundImageUrl"); }
    public void setBackgroundImageUrl(String backgroundImageUrl) { profile.put("backgroundImageUrl", backgroundImageUrl); }

    public String getCity() { return (String) profile.get("city"); }
    public void setCity(String city) { profile.put("city", city); }

    public String getBio() { return (String) profile.get("bio"); }
    public void setBio(String bio) { profile.put("bio", bio); }

    public Timestamp getBirthDate() { return (Timestamp) profile.get("birthDate"); }
    public void setBirthDate(Timestamp birthDate) { profile.put("birthDate", birthDate); }

    @SuppressWarnings("unchecked")
    public Map<String, String> getSocialLinks() { 
        return (Map<String, String>) profile.getOrDefault("socialLinks", new HashMap<String, String>());
    }
    
    public void setSocialLinks(Map<String, String> socialLinks) {
        profile.put("socialLinks", socialLinks);
    }

    // Stats getters and setters
    public double getRating() { return (double) stats.getOrDefault("rating", 0.0); }
    public void setRating(double rating) { stats.put("rating", rating); }

    public int getEventsAttended() { return (int) stats.getOrDefault("eventsAttended", 0); }
    public void setEventsAttended(int eventsAttended) { stats.put("eventsAttended", eventsAttended); }

    public int getOrganizedEvents() { return (int) stats.getOrDefault("organizedEvents", 0); }
    public void setOrganizedEvents(int organizedEvents) { stats.put("organizedEvents", organizedEvents); }

    public int getFollowersCount() { return (int) stats.getOrDefault("followersCount", 0); }
    public void setFollowersCount(int followersCount) { stats.put("followersCount", followersCount); }

    public int getFollowingCount() { return (int) stats.getOrDefault("followingCount", 0); }
    public void setFollowingCount(int followingCount) { stats.put("followingCount", followingCount); }

    public Timestamp getLastEventDate() { return (Timestamp) stats.get("lastEventDate"); }
    public void setLastEventDate(Timestamp lastEventDate) { stats.put("lastEventDate", lastEventDate); }

    // Helper methods
    public void addInterest(String interest) {
        if (!interests.contains(interest)) {
            interests.add(interest);
        }
    }

    public void removeInterest(String interest) {
        interests.remove(interest);
    }

    public void addSearchKeyword(String keyword) {
        if (!searchKeywords.contains(keyword)) {
            searchKeywords.add(keyword);
        }
    }

    public void removeSearchKeyword(String keyword) {
        searchKeywords.remove(keyword);
    }

    public void addSocialLink(String platform, String url) {
        Map<String, String> socialLinks = getSocialLinks();
        socialLinks.put(platform, url);
        setSocialLinks(socialLinks);
    }

    public void removeSocialLink(String platform) {
        Map<String, String> socialLinks = getSocialLinks();
        socialLinks.remove(platform);
        setSocialLinks(socialLinks);
    }

    public void updateStat(String key, Object value) {
        stats.put(key, value);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("email", email);
        map.put("passwordHash", passwordHash);
        map.put("username", username);
        map.put("customTag", customTag);
        map.put("profile", profile);
        map.put("interests", interests);
        map.put("searchKeywords", searchKeywords);
        map.put("accountType", accountType);
        map.put("isAdmin", isAdmin);
        map.put("isVerified", isVerified);
        map.put("isBanned", isBanned);
        map.put("stats", stats);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
} 