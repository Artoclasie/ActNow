package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

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
    private String accountType;
    private String role;
    private Map<String, Object> stats;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String avatarUrl;
    private String backgroundImageUrl;
    private String city;
    private Timestamp birthDate;
    private int themeMode;

    public UserProfile() {
        this.profile = new HashMap<>();
        this.interests = new ArrayList<>();
        this.searchKeywords = new ArrayList<>();
        this.stats = new HashMap<>();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();

        stats.put("postsCount", 0);
        stats.put("eventsAttended", 0);
        stats.put("eventsRegistered", 0);
        stats.put("organizedEvents", 0);
        stats.put("volunteerHours", 0);
        stats.put("followersCount", 0);
        stats.put("followingCount", 0);
        stats.put("rating", 0.0);
    }

    public UserProfile(String userId, String email, String username, String accountType) {
        this();
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.accountType = accountType;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }

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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBackgroundImageUrl() { return backgroundImageUrl; }
    public void setBackgroundImageUrl(String backgroundImageUrl) { this.backgroundImageUrl = backgroundImageUrl; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Timestamp getBirthDate() { return birthDate; }
    public void setBirthDate(Timestamp birthDate) { this.birthDate = birthDate; }


    public String getName() { return username; }
    public void setName(String name) { profile.put("name", name); this.username = name; }

    public String getProfileImage() { return avatarUrl != null ? avatarUrl : (String) profile.get("avatarUrl"); }
    public void setProfileImage(String avatarUrl) { profile.put("avatarUrl", avatarUrl); this.avatarUrl = avatarUrl; }

    public String getBio() { return (String) profile.get("bio"); }
    public void setBio(String bio) { profile.put("bio", bio); }

    @SuppressWarnings("unchecked")
    public Map<String, String> getSocialLinks() {
        return (Map<String, String>) profile.getOrDefault("socialLinks", new HashMap<String, String>());
    }

    public void setSocialLinks(Map<String, String> socialLinks) {
        profile.put("socialLinks", socialLinks);
    }

    public double getRating() { return (double) stats.getOrDefault("rating", 0.0); }
    public void setRating(double rating) { stats.put("rating", rating); }

    public int getEventsAttended() {
        Number value = (Number) stats.getOrDefault("eventsAttended", 0);
        return value != null ? value.intValue() : 0;
    }
    public void setEventsAttended(int eventsAttended) { stats.put("eventsAttended", eventsAttended); }

    public int getOrganizedEvents() {
        Number value = (Number) stats.getOrDefault("organizedEvents", 0);
        return value != null ? value.intValue() : 0;
    }
    public void setOrganizedEvents(int organizedEvents) { stats.put("organizedEvents", organizedEvents); }

    public int getPostsCount() {
        Number value = (Number) stats.getOrDefault("postsCount", 0);
        return value != null ? value.intValue() : 0;
    }
    public void setPostsCount(int postsCount) { stats.put("postsCount", postsCount); }

    public int getVolunteerHours() {
        Number value = (Number) stats.getOrDefault("volunteerHours", 0);
        return value != null ? value.intValue() : 0;
    }
    public void setVolunteerHours(int volunteerHours) { stats.put("volunteerHours", volunteerHours); }

    public int getFollowersCount() {
        Number value = (Number) stats.getOrDefault("followersCount", 0);
        return value != null ? value.intValue() : 0;
    }
    public void setFollowersCount(int followersCount) { stats.put("followersCount", followersCount); }

    public int getFollowingCount() {
        Number value = (Number) stats.getOrDefault("followingCount", 0);
        return value != null ? value.intValue() : 0;
    }
    public void setFollowingCount(int followingCount) { stats.put("followingCount", followingCount); }

    public Timestamp getLastEventDate() { return (Timestamp) stats.get("lastEventDate"); }
    public void setLastEventDate(Timestamp lastEventDate) { stats.put("lastEventDate", lastEventDate); }


    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("email", email);
        map.put("passwordHash", passwordHash);
        map.put("username", username);
        map.put("birthDate", birthDate);
        map.put("customTag", customTag);
        map.put("profile", profile);
        map.put("interests", interests);
        map.put("searchKeywords", searchKeywords);
        map.put("accountType", accountType);
        map.put("role", role);
        map.put("stats", stats);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("avatarUrl", avatarUrl);
        map.put("backgroundImageUrl", backgroundImageUrl);
        map.put("city", city);
        map.put("themeMode", themeMode);
        return map;
    }
}