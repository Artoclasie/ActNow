package com.example.actnow.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamModel {
    private String teamId;
    private String name;
    private String creatorId;
    private String description;
    private List<String> members;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public TeamModel() {
        this.members = new ArrayList<>();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public TeamModel(String teamId, String name, String creatorId, String description) {
        this();
        this.teamId = teamId;
        this.name = name;
        this.creatorId = creatorId;
        this.description = description;
        this.members.add(creatorId);
    }

    // Getters and Setters
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void addMember(String userId) {
        if (!members.contains(userId)) {
            members.add(userId);
            updatedAt = Timestamp.now();
        }
    }

    public void removeMember(String userId) {
        if (!userId.equals(creatorId) && members.remove(userId)) {
            updatedAt = Timestamp.now();
        }
    }

    public boolean isMember(String userId) {
        return members.contains(userId);
    }

    public boolean isCreator(String userId) {
        return creatorId.equals(userId);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("teamId", teamId);
        map.put("name", name);
        map.put("creatorId", creatorId);
        map.put("description", description);
        map.put("members", members);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
} 