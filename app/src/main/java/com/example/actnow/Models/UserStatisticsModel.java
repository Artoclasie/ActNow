package com.example.actnow.Models;

public class UserStatisticsModel {
    private int eventsCreated;
    private int eventsParticipated;
    private int eventsCancelled;
    private int totalFollowers;
    private int totalFollowing;
    private int totalInterests;
    private int totalSavedEvents;
    private long lastActive;
    private double averageRating;
    private int totalRatings;

    public UserStatisticsModel() {
        this.eventsCreated = 0;
        this.eventsParticipated = 0;
        this.eventsCancelled = 0;
        this.totalFollowers = 0;
        this.totalFollowing = 0;
        this.totalInterests = 0;
        this.totalSavedEvents = 0;
        this.lastActive = System.currentTimeMillis();
        this.averageRating = 0.0;
        this.totalRatings = 0;
    }

    public int getEventsCreated() {
        return eventsCreated;
    }

    public void setEventsCreated(int eventsCreated) {
        this.eventsCreated = eventsCreated;
    }

    public int getEventsParticipated() {
        return eventsParticipated;
    }

    public void setEventsParticipated(int eventsParticipated) {
        this.eventsParticipated = eventsParticipated;
    }

    public int getEventsCancelled() {
        return eventsCancelled;
    }

    public void setEventsCancelled(int eventsCancelled) {
        this.eventsCancelled = eventsCancelled;
    }

    public int getTotalFollowers() {
        return totalFollowers;
    }

    public void setTotalFollowers(int totalFollowers) {
        this.totalFollowers = totalFollowers;
    }

    public int getTotalFollowing() {
        return totalFollowing;
    }

    public void setTotalFollowing(int totalFollowing) {
        this.totalFollowing = totalFollowing;
    }

    public int getTotalInterests() {
        return totalInterests;
    }

    public void setTotalInterests(int totalInterests) {
        this.totalInterests = totalInterests;
    }

    public int getTotalSavedEvents() {
        return totalSavedEvents;
    }

    public void setTotalSavedEvents(int totalSavedEvents) {
        this.totalSavedEvents = totalSavedEvents;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    // Helper methods
    public void incrementEventsCreated() {
        this.eventsCreated++;
    }

    public void incrementEventsParticipated() {
        this.eventsParticipated++;
    }

    public void incrementEventsCancelled() {
        this.eventsCancelled++;
    }

    public void incrementTotalFollowers() {
        this.totalFollowers++;
    }

    public void decrementTotalFollowers() {
        if (this.totalFollowers > 0) {
            this.totalFollowers--;
        }
    }

    public void incrementTotalFollowing() {
        this.totalFollowing++;
    }

    public void decrementTotalFollowing() {
        if (this.totalFollowing > 0) {
            this.totalFollowing--;
        }
    }

    public void incrementTotalInterests() {
        this.totalInterests++;
    }

    public void decrementTotalInterests() {
        if (this.totalInterests > 0) {
            this.totalInterests--;
        }
    }

    public void incrementTotalSavedEvents() {
        this.totalSavedEvents++;
    }

    public void decrementTotalSavedEvents() {
        if (this.totalSavedEvents > 0) {
            this.totalSavedEvents--;
        }
    }

    public void updateLastActive() {
        this.lastActive = System.currentTimeMillis();
    }

    public void addRating(double rating) {
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        double totalRating = this.averageRating * this.totalRatings;
        this.totalRatings++;
        this.averageRating = (totalRating + rating) / this.totalRatings;
    }
} 