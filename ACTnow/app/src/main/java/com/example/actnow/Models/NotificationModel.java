package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class NotificationModel {
    private String notificationId;
    private String userId;
    private String type; // event_invite, event_update, event_cancel, mention, follow, like, comment, system
    private String title;
    private String message;
    private Map<String, String> data;
    private boolean isRead;
    private Timestamp createdAt;

    public NotificationModel() {
        this.data = new HashMap<>();
        this.isRead = false;
        this.createdAt = Timestamp.now();
    }

    public NotificationModel(String notificationId, String userId, String type, String title, String message) {
        this();
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
    }

    // Getters and Setters
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) { this.data = data; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Data getters and setters
    public String getEventId() { return data.get("eventId"); }
    public void setEventId(String eventId) { data.put("eventId", eventId); }

    public String getPostId() { return data.get("postId"); }
    public void setPostId(String postId) { data.put("postId", postId); }

    public String getCommentId() { return data.get("commentId"); }
    public void setCommentId(String commentId) { data.put("commentId", commentId); }

    public String getMentionedBy() { return data.get("mentionedBy"); }
    public void setMentionedBy(String mentionedBy) { data.put("mentionedBy", mentionedBy); }

    // Helper methods
    public void addData(String key, String value) {
        data.put(key, value);
    }

    public void removeData(String key) {
        data.remove(key);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("notificationId", notificationId);
        map.put("userId", userId);
        map.put("type", type);
        map.put("title", title);
        map.put("message", message);
        map.put("data", data);
        map.put("isRead", isRead);
        map.put("createdAt", createdAt);
        return map;
    }
}
