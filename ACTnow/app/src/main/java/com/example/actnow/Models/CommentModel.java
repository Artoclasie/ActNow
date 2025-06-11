package com.example.actnow.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentModel {
    private String commentId;
    private String postId;
    private String authorId;
    private String authorName;
    private String content;
    private List<String> imageUrls;
    private List<String> mentionedUsers;
    private Map<String, Boolean> likes;
    private int repliesCount;
    private boolean isHidden;
    private Object createdAt;
    private Object updatedAt;

    public CommentModel() {
        // Default constructor for Firebase
    }

    public CommentModel(String commentId, String postId, String authorId, String content) {
        this.commentId = commentId;
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.imageUrls = new ArrayList<>();
        this.mentionedUsers = new ArrayList<>();
        this.likes = new HashMap<>();
        this.repliesCount = 0;
        this.isHidden = false;
        this.createdAt = ServerValue.TIMESTAMP;
        this.updatedAt = ServerValue.TIMESTAMP;
    }

    // Getters and setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getMentionedUsers() { return mentionedUsers; }
    public void setMentionedUsers(List<String> mentionedUsers) { this.mentionedUsers = mentionedUsers; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }

    public int getRepliesCount() { return repliesCount; }
    public void setRepliesCount(int repliesCount) { this.repliesCount = repliesCount; }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }

    public Object getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }

    public Object getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }

    @Exclude
    public long getCreatedAtLong() {
        if (createdAt instanceof Long) {
            return (Long) createdAt;
        }
        return 0;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("commentId", commentId);
        result.put("postId", postId);
        result.put("authorId", authorId);
        result.put("authorName", authorName);
        result.put("content", content);
        result.put("imageUrls", imageUrls);
        result.put("mentionedUsers", mentionedUsers);
        result.put("likes", likes);
        result.put("repliesCount", repliesCount);
        result.put("isHidden", isHidden);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }

    // Helper methods
    public void addLike(String userId) {
        if (likes == null) {
            likes = new HashMap<>();
        }
        likes.put(userId, true);
    }

    public void removeLike(String userId) {
        if (likes != null) {
            likes.remove(userId);
        }
    }

    public boolean isLikedBy(String userId) {
        return likes != null && likes.containsKey(userId);
    }

    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }
}