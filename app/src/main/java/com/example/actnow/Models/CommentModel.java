package com.example.actnow.Models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentModel {
    private String commentId;
    private String eventId;
    private String authorId;
    private String authorName;
    private String profileImage;
    private String content;
    private List<String> imageUrls;
    private List<String> mentionedUsers;
    private Map<String, Boolean> likes;
    private int likesCount;
    private int repliesCount;
    private boolean isHidden;
    private com.google.firebase.Timestamp createdAt;
    private com.google.firebase.Timestamp updatedAt;

    public CommentModel() {
        this.imageUrls = new ArrayList<>();
        this.mentionedUsers = new ArrayList<>();
        this.likes = new HashMap<>();
        this.likesCount = 0;
        this.repliesCount = 0;
        this.isHidden = false;
    }

    public CommentModel(String commentId, String eventId, String authorId, String authorName, String profileImage, String content) {
        this();
        this.commentId = commentId;
        this.eventId = eventId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.profileImage = profileImage;
        this.content = content;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<String> getMentionedUsers() { return mentionedUsers; }
    public void setMentionedUsers(List<String> mentionedUsers) { this.mentionedUsers = mentionedUsers; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
        this.likesCount = likes != null ? likes.size() : 0;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = Math.max(0, likesCount);
    }

    public int getRepliesCount() { return repliesCount; }
    public void setRepliesCount(int repliesCount) { this.repliesCount = repliesCount; }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }

    @ServerTimestamp
    public com.google.firebase.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(com.google.firebase.Timestamp createdAt) { this.createdAt = createdAt; }

    @ServerTimestamp
    public com.google.firebase.Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(com.google.firebase.Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Exclude
    public long getCreatedAtLong() {
        return createdAt != null ? createdAt.toDate().getTime() : 0;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("commentId", commentId);
        result.put("eventId", eventId);
        result.put("authorId", authorId);
        result.put("authorName", authorName);
        result.put("profileImage", profileImage);
        result.put("content", content);
        result.put("imageUrls", imageUrls);
        result.put("mentionedUsers", mentionedUsers);
        result.put("likes", likes);
        result.put("likesCount", likesCount);
        result.put("repliesCount", repliesCount);
        result.put("isHidden", isHidden);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }

    public void addLike(String userId) {
        if (likes == null) {
            likes = new HashMap<>();
        }
        if (!likes.containsKey(userId)) {
            likes.put(userId, true);
            likesCount++;
        }
    }

    public void removeLike(String userId) {
        if (likes != null && likes.containsKey(userId)) {
            likes.remove(userId);
            likesCount = Math.max(0, likesCount - 1);
        }
    }

    public boolean isLikedBy(String userId) {
        return likes != null && likes.containsKey(userId);
    }
}