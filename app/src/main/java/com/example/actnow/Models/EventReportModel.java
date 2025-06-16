package com.example.actnow.Models;

import java.util.ArrayList;
import java.util.List;

public class EventReportModel {
    private String id;
    private String eventId;
    private String organizerId;
    private String eventName;
    private long eventDate;
    private int totalParticipants;
    private int totalAttended;
    private double averageRating;
    private List<String> feedbackComments;
    private List<String> photos;
    private String summary;
    private double revenue;
    private double expenses;
    private List<String> keyMetrics;
    private long createdAt;

    public EventReportModel() {
        this.feedbackComments = new ArrayList<>();
        this.photos = new ArrayList<>();
        this.keyMetrics = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public EventReportModel(String eventId, String organizerId, String eventName, long eventDate) {
        this();
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.eventName = eventName;
        this.eventDate = eventDate;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public int getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(int totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public int getTotalAttended() {
        return totalAttended;
    }

    public void setTotalAttended(int totalAttended) {
        this.totalAttended = totalAttended;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public List<String> getFeedbackComments() {
        return feedbackComments;
    }

    public void setFeedbackComments(List<String> feedbackComments) {
        this.feedbackComments = feedbackComments;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getExpenses() {
        return expenses;
    }

    public void setExpenses(double expenses) {
        this.expenses = expenses;
    }

    public List<String> getKeyMetrics() {
        return keyMetrics;
    }

    public void setKeyMetrics(List<String> keyMetrics) {
        this.keyMetrics = keyMetrics;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void addFeedbackComment(String comment) {
        if (comment != null && !comment.isEmpty()) {
            feedbackComments.add(comment);
        }
    }

    public void addPhoto(String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty()) {
            photos.add(photoUrl);
        }
    }

    public void addKeyMetric(String metric) {
        if (metric != null && !metric.isEmpty()) {
            keyMetrics.add(metric);
        }
    }

    public double getProfit() {
        return revenue - expenses;
    }

    public double getAttendanceRate() {
        if (totalParticipants == 0) return 0;
        return (double) totalAttended / totalParticipants * 100;
    }
} 