package com.example.actnow.Models;

import java.util.Map;

public class ProfileModel {
    private String uid;
    private String username;
    private String email;
    private String profileImageUrl;
    private String city;
    private Long countPost;
    private Long followersCount;
    private Long followingCount;
    private Map<String, Boolean> subscriptions;
    private String lastMessage; // Временное поле для отображения
    private long lastMessageTimestamp;// Для хранения подписок

    public ProfileModel(String uid, String username, String email, String profileImageUrl, String city, Long countPost, Long followersCount, Long followingCount, Map<String, Boolean> subscriptions) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.city = city;
        this.countPost = countPost;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.subscriptions = subscriptions;
    }

    public Map<String, Boolean> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Map<String, Boolean> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Long getCountPost() {
        return countPost;
    }

    public void setCountPost(Long countPost) {
        this.countPost = countPost;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    // Пустой конструктор нужен для Firestore
    public ProfileModel() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String userId) {
        this.uid = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}
