package com.example.actnow.Models;

import java.util.Date;

public class ReviewModel {
    private String id;
    private String userId;
    private String reviewerId;
    private String eventId;
    private float rating;
    private String content;
    private Date createdAt;

    // Constructors, getters, and setters
    public ReviewModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}