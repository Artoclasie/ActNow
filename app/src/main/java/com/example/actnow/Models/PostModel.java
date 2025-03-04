package com.example.actnow.Models;

import java.util.List;

public class PostModel {
    private String postId;
    private String userId;
    private String title;
    private String content;
    private String imageUrl;
    private long publishDate;
    private List<String> likes;
    private List<CommentsModel> comments;  // Изменено на List<CommentsModel>
    private List<String> participants;
    private String location;  // Новое поле: место
    private String date;       // Новое поле: дата
    private String city;       // Новое поле: город
    private String description; // Новое поле: описание

    // Пустой конструктор для Firestore
    public PostModel() {}

    public PostModel(String postId, String userId, String title, String content, String imageUrl, long publishDate,
                     List<String> likes, List<CommentsModel> comments, List<String> participants,
                     String location, String date, String city, String description) {  // Добавлены новые поля
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.publishDate = publishDate;
        this.likes = likes;
        this.comments = comments;
        this.participants = participants;
        this.location = location;  // Инициализация нового поля
        this.date = date;          // Инициализация нового поля
        this.city = city;         // Инициализация нового поля
        this.description = description; // Инициализация нового поля
    }

    // Геттеры и сеттеры
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getPublishDate() { return publishDate; }
    public void setPublishDate(long publishDate) { this.publishDate = publishDate; }

    public List<String> getLikes() { return likes; }
    public void setLikes(List<String> likes) { this.likes = likes; }

    public List<CommentsModel> getComments() { return comments; }
    public void setComments(List<CommentsModel> comments) { this.comments = comments; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLocation() { return location; }  // Геттер для места
    public void setLocation(String location) { this.location = location; }  // Сеттер для места

    public String getDate() { return date; }  // Геттер для даты
    public void setDate(String date) { this.date = date; }  // Сеттер для даты

    public String getCity() { return city; }  // Геттер для города
    public void setCity(String city) { this.city = city; }  // Сеттер для города

    public String getDescription() { return description; }  // Геттер для описания
    public void setDescription(String description) { this.description = description; }  // Сеттер для описания
}