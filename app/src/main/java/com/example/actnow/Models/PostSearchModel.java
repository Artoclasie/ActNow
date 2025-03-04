package com.example.actnow.Models;

import java.util.ArrayList;

public class PostSearchModel {
    private String id;
    private String content;
    private String imageUrl;
    private String uid;
    private String timestamp;
    private String username;
    private String profileImageUrl;
    private ArrayList<String> likes;
    private ArrayList<String> comments;

    public PostSearchModel() { }

    public PostSearchModel(String id, String content, String imageUrl, String uid, String timestamp, String username, String profileImageUrl, ArrayList<String> likes, ArrayList<String> comments) {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.timestamp = timestamp;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.likes = likes;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public ArrayList<String> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<String> likes) {
        this.likes = likes;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }
}
