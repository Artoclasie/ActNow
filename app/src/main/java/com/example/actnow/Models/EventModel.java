package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventModel {
    private String eventId;
    private String title;
    private String description;
    private Integer currentParticipants; // Изменено с int на Integer
    private Integer maxParticipants;     // Изменено с int на Integer
    private int minAge;
    private Timestamp startDate;
    private Timestamp endDate;
    private String startTime;
    private String endTime;
    private Map<String, Object> location;
    private List<String> tags;
    private String organizerId;
    private String status;
    private List<String> searchKeywords;
    private Map<String, Object> images;
    private List<String> schedule;
    private List<String> featuredReports;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private double averageRating;

    // Конструктор по умолчанию
    public EventModel() {
        this.searchKeywords = new ArrayList<>();
        this.location = new HashMap<>();
        this.schedule = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.images = new HashMap<>();
        this.featuredReports = new ArrayList<>();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.startDate = Timestamp.now();
        this.endDate = Timestamp.now();
        this.startTime = "09:00";
        this.endTime = "10:00";
        this.currentParticipants = 0; // Инициализация значением по умолчанию
        this.maxParticipants = 100;   // Инициализация значением по умолчанию
        this.minAge = 15;
        this.status = "draft";
        this.averageRating = 0.0;
    }

    // Конструктор с основными полями
    public EventModel(String eventId, String organizerId, String title, String description) {
        this();
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
    }

    // Геттеры и сеттеры
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(Integer currentParticipants) { this.currentParticipants = currentParticipants; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) { this.minAge = minAge; }
    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Map<String, Object> getLocation() { return location; }
    public void setLocation(Map<String, Object> location) { this.location = location; }
    public String getAddress() { return location != null ? (String) location.get("address") : null; }
    public void setAddress(String address) { if (location == null) location = new HashMap<>(); location.put("address", address); }
    public String getCity() { return location != null ? (String) location.get("city") : null; }
    public void setCity(String city) { if (location == null) location = new HashMap<>(); location.put("city", city); }
    public GeoPoint getCoordinates() { return location != null ? (GeoPoint) location.get("coordinates") : null; }
    public void setCoordinates(GeoPoint coordinates) { if (location == null) location = new HashMap<>(); location.put("coordinates", coordinates); }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }
    public Map<String, Object> getImages() { return images; }
    public void setImages(Map<String, Object> images) { this.images = images; }
    public String getCoverImage() { return images != null ? (String) images.get("coverImage") : null; }
    public void setCoverImage(String coverImage) { if (images == null) images = new HashMap<>(); images.put("coverImage", coverImage); }
    public List<String> getGallery() { return images != null ? (List<String>) images.getOrDefault("gallery", new ArrayList<>()) : new ArrayList<>(); }
    public void setGallery(List<String> gallery) { if (images == null) images = new HashMap<>(); images.put("gallery", gallery); }
    public List<String> getSchedule() { return schedule; }
    public void setSchedule(List<String> schedule) { this.schedule = schedule; }
    public List<String> getFeaturedReports() { return featuredReports; }
    public void setFeaturedReports(List<String> featuredReports) { this.featuredReports = featuredReports; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", eventId);
        map.put("title", title);
        map.put("description", description);
        map.put("currentParticipants", currentParticipants);
        map.put("maxParticipants", maxParticipants);
        map.put("minAge", minAge);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("location", location);
        map.put("tags", tags);
        map.put("organizerId", organizerId);
        map.put("status", status);
        map.put("searchKeywords", searchKeywords);
        map.put("images", images);
        map.put("schedule", schedule);
        map.put("featuredReports", featuredReports);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("averageRating", averageRating);
        return map;
    }
}