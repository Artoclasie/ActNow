package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PostModel {
    private String postId;
    private String authorId;
    private String content;
    private List<String> searchKeywords;
    private List<String> images;
    private List<String> tags;
    private String linkedEventId;
    private boolean isReport;
    private boolean isHidden;
    private int likesCount;
    private int commentsCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public PostModel() {
        this.searchKeywords = new ArrayList<>();
        this.images = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.isReport = false;
        this.isHidden = false;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public PostModel(String postId, String authorId, String content) {
        this();
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
    }

    // Getters and Setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getLinkedEventId() { return linkedEventId; }
    public void setLinkedEventId(String linkedEventId) { this.linkedEventId = linkedEventId; }

    public boolean isReport() { return isReport; }
    public void setReport(boolean report) { isReport = report; }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void addSearchKeyword(String keyword) {
        if (!searchKeywords.contains(keyword)) {
            searchKeywords.add(keyword);
        }
    }

    public void removeSearchKeyword(String keyword) {
        searchKeywords.remove(keyword);
    }

    public void addImage(String imageUrl) {
        if (images.size() < 5 && !images.contains(imageUrl)) {
            images.add(imageUrl);
        }
    }

    public void removeImage(String imageUrl) {
        images.remove(imageUrl);
    }

    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void incrementLikes() {
        likesCount++;
    }

    public void decrementLikes() {
        if (likesCount > 0) {
            likesCount--;
        }
    }

    public void incrementComments() {
        commentsCount++;
    }

    public void decrementComments() {
        if (commentsCount > 0) {
            commentsCount--;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("authorId", authorId);
        map.put("content", content);
        map.put("searchKeywords", searchKeywords);
        map.put("images", images);
        map.put("tags", tags);
        map.put("linkedEventId", linkedEventId);
        map.put("isReport", isReport);
        map.put("isHidden", isHidden);
        map.put("likesCount", likesCount);
        map.put("commentsCount", commentsCount);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}