package com.example.actnow.Models;

import java.util.List;

public class CommentsModel {
    private String userId;
    private String commentText;
    private String timestamp;
    private List<String> likes;

    public CommentsModel() {}

    public CommentsModel(String userId, String commentText, String timestamp, List<String> likes) {
        this.userId = userId;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.likes = likes;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public List<String> getLikes() { return likes; }
    public void setLikes(List<String> likes) { this.likes = likes; }
}
