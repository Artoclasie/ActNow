package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventModel {
    private String eventId;
    private String organizerId;
    private String title;
    private String description;
    private List<String> searchKeywords;
    private Map<String, Object> location;
    private String onlineLink;
    private Timestamp startDate;
    private Timestamp endDate;
    private List<Map<String, Object>> schedule;
    private int maxParticipants;
    private int currentParticipants;
    private List<String> tags;
    private String status; // draft, pending, published, canceled, completed
    private Map<String, Object> ageRestriction;
    private boolean isManualApproval;
    private Map<String, Object> images;
    private List<String> featuredReports;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public EventModel() {
        this.searchKeywords = new ArrayList<>();
        this.location = new HashMap<>();
        this.schedule = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.ageRestriction = new HashMap<>();
        this.images = new HashMap<>();
        this.featuredReports = new ArrayList<>();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public EventModel(String eventId, String organizerId, String title, String description) {
        this();
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.status = "draft";
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(List<String> searchKeywords) { this.searchKeywords = searchKeywords; }

    public Map<String, Object> getLocation() { return location; }
    public void setLocation(Map<String, Object> location) { this.location = location; }

    public String getOnlineLink() { return onlineLink; }
    public void setOnlineLink(String onlineLink) { this.onlineLink = onlineLink; }

    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public List<Map<String, Object>> getSchedule() { return schedule; }
    public void setSchedule(List<Map<String, Object>> schedule) { this.schedule = schedule; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, Object> getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(Map<String, Object> ageRestriction) { this.ageRestriction = ageRestriction; }

    public boolean isManualApproval() { return isManualApproval; }
    public void setManualApproval(boolean manualApproval) { isManualApproval = manualApproval; }

    public Map<String, Object> getImages() { return images; }
    public void setImages(Map<String, Object> images) { this.images = images; }

    public List<String> getFeaturedReports() { return featuredReports; }
    public void setFeaturedReports(List<String> featuredReports) { this.featuredReports = featuredReports; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Location getters and setters
    public String getAddress() { return (String) location.get("address"); }
    public void setAddress(String address) { location.put("address", address); }

    public GeoPoint getCoordinates() { return (GeoPoint) location.get("coordinates"); }
    public void setCoordinates(GeoPoint coordinates) { location.put("coordinates", coordinates); }

    // Age restriction getters and setters
    public int getMinAge() { return (int) ageRestriction.getOrDefault("minAge", 15); }
    public void setMinAge(int minAge) { ageRestriction.put("minAge", minAge); }

    public boolean isAdultsOnly() { return (boolean) ageRestriction.getOrDefault("isAdultsOnly", false); }
    public void setAdultsOnly(boolean adultsOnly) { ageRestriction.put("isAdultsOnly", adultsOnly); }

    public String getAgeRestrictionDescription() { return (String) ageRestriction.get("description"); }
    public void setAgeRestrictionDescription(String description) { ageRestriction.put("description", description); }

    // Images getters and setters
    public String getCoverImage() { return (String) images.get("coverImage"); }
    public void setCoverImage(String coverImage) { images.put("coverImage", coverImage); }

    public List<String> getGallery() { return (List<String>) images.getOrDefault("gallery", new ArrayList<>()); }
    public void setGallery(List<String> gallery) { images.put("gallery", gallery); }

    // Helper methods
    public void addSearchKeyword(String keyword) {
        if (!searchKeywords.contains(keyword)) {
            searchKeywords.add(keyword);
        }
    }

    public void removeSearchKeyword(String keyword) {
        searchKeywords.remove(keyword);
    }

    public void addScheduleItem(Timestamp date, boolean isCanceled, String cancelReason) {
        Map<String, Object> item = new HashMap<>();
        item.put("date", date);
        item.put("isCanceled", isCanceled);
        item.put("cancelReason", cancelReason);
        schedule.add(item);
    }

    public void removeScheduleItem(int index) {
        if (index >= 0 && index < schedule.size()) {
            schedule.remove(index);
        }
    }

    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void addGalleryImage(String imageUrl) {
        List<String> gallery = getGallery();
        if (!gallery.contains(imageUrl)) {
            gallery.add(imageUrl);
            setGallery(gallery);
        }
    }

    public void removeGalleryImage(String imageUrl) {
        List<String> gallery = getGallery();
        gallery.remove(imageUrl);
        setGallery(gallery);
    }

    public void addFeaturedReport(String reportId) {
        if (!featuredReports.contains(reportId)) {
            featuredReports.add(reportId);
        }
    }

    public void removeFeaturedReport(String reportId) {
        featuredReports.remove(reportId);
    }

    public void incrementParticipants() {
        if (currentParticipants < maxParticipants) {
            currentParticipants++;
        }
    }

    public void decrementParticipants() {
        if (currentParticipants > 0) {
            currentParticipants--;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", eventId);
        map.put("organizerId", organizerId);
        map.put("title", title);
        map.put("description", description);
        map.put("searchKeywords", searchKeywords);
        map.put("location", location);
        map.put("onlineLink", onlineLink);
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        map.put("schedule", schedule);
        map.put("maxParticipants", maxParticipants);
        map.put("currentParticipants", currentParticipants);
        map.put("tags", tags);
        map.put("status", status);
        map.put("ageRestriction", ageRestriction);
        map.put("isManualApproval", isManualApproval);
        map.put("images", images);
        map.put("featuredReports", featuredReports);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
