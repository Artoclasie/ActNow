package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EventParticipantModel {
    private String participationId;
    private String eventId;
    private String userId;
    private String status; // pending, approved, rejected, attended, no_show
    private int groupSize;
    private boolean isGroupLeader;
    private List<Map<String, Object>> groupMembers;
    private List<Map<String, Object>> answers;
    private Map<String, Object> feedback;
    private String teamId;
    private Timestamp registeredAt;
    private Timestamp approvedAt;
    private String rejectedReason;

    public EventParticipantModel() {
        this.groupSize = 1;
        this.isGroupLeader = false;
        this.groupMembers = new ArrayList<>();
        this.answers = new ArrayList<>();
        this.feedback = new HashMap<>();
        this.registeredAt = Timestamp.now();
    }

    public EventParticipantModel(String participationId, String eventId, String userId) {
        this();
        this.participationId = participationId;
        this.eventId = eventId;
        this.userId = userId;
        this.status = "pending";
    }

    // Getters and Setters
    public String getParticipationId() { return participationId; }
    public void setParticipationId(String participationId) { this.participationId = participationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getGroupSize() { return groupSize; }
    public void setGroupSize(int groupSize) { this.groupSize = groupSize; }

    public boolean isGroupLeader() { return isGroupLeader; }
    public void setGroupLeader(boolean groupLeader) { isGroupLeader = groupLeader; }

    public List<Map<String, Object>> getGroupMembers() { return groupMembers; }
    public void setGroupMembers(List<Map<String, Object>> groupMembers) { this.groupMembers = groupMembers; }

    public List<Map<String, Object>> getAnswers() { return answers; }
    public void setAnswers(List<Map<String, Object>> answers) { this.answers = answers; }

    public Map<String, Object> getFeedback() { return feedback; }
    public void setFeedback(Map<String, Object> feedback) { this.feedback = feedback; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public Timestamp getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Timestamp registeredAt) { this.registeredAt = registeredAt; }

    public Timestamp getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Timestamp approvedAt) { this.approvedAt = approvedAt; }

    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }

    // Helper methods
    public void addGroupMember(String name, int age) {
        Map<String, Object> member = new HashMap<>();
        member.put("name", name);
        member.put("age", age);
        groupMembers.add(member);
        groupSize = groupMembers.size() + 1;
    }

    public void removeGroupMember(int index) {
        if (index >= 0 && index < groupMembers.size()) {
            groupMembers.remove(index);
            groupSize = groupMembers.size() + 1;
        }
    }

    public void addAnswer(String questionId, Object answer) {
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("questionId", questionId);
        answerMap.put("answer", answer);
        answers.add(answerMap);
    }

    public void setFeedbackRating(int rating) {
        feedback.put("rating", rating);
        feedback.put("updatedAt", Timestamp.now());
    }

    public void setFeedbackText(String text) {
        feedback.put("text", text);
        feedback.put("updatedAt", Timestamp.now());
    }

    public void addFeedbackImage(String imageUrl) {
        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) feedback.getOrDefault("images", new ArrayList<String>());
        images.add(imageUrl);
        feedback.put("images", images);
        feedback.put("updatedAt", Timestamp.now());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("participationId", participationId);
        map.put("eventId", eventId);
        map.put("userId", userId);
        map.put("status", status);
        map.put("groupSize", groupSize);
        map.put("isGroupLeader", isGroupLeader);
        map.put("groupMembers", groupMembers);
        map.put("answers", answers);
        map.put("feedback", feedback);
        map.put("teamId", teamId);
        map.put("registeredAt", registeredAt);
        map.put("approvedAt", approvedAt);
        map.put("rejectedReason", rejectedReason);
        return map;
    }
} 