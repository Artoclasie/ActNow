package com.example.actnow.Models;

import com.google.firebase.Timestamp;

public class ReviewModel {
    private String id;
    private String eventId;
    private String reviewerId;
    private String userId;
    private String content;
    private Timestamp createdAt;
    private float rating;
    private String eventTitle; // Добавлено поле для заголовка мероприятия

    // Конструкторы
    public ReviewModel() {}

    public ReviewModel(String id, String eventId, String reviewerId, String userId, String content, Timestamp createdAt, float rating) {
        this.id = id;
        this.eventId = eventId;
        this.reviewerId = reviewerId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.rating = rating;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getEventTitle() { return eventTitle; } // Геттер
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; } // Сеттер
}