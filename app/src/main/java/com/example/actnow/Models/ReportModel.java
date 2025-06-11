package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import java.util.Map;
import java.util.HashMap;

public class ReportModel {
    private String reportId;
    private String reporterId;
    private String targetType; // user, post, event, comment
    private String targetId;
    private String reason; // spam, fraud, inappropriate
    private String status; // pending, resolved, rejected
    private Timestamp createdAt;

    public ReportModel() {
        this.status = "pending";
        this.createdAt = Timestamp.now();
    }

    public ReportModel(String reportId, String reporterId, String targetType, String targetId, String reason) {
        this();
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }

    // Getters and Setters
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("reportId", reportId);
        map.put("reporterId", reporterId);
        map.put("targetType", targetType);
        map.put("targetId", targetId);
        map.put("reason", reason);
        map.put("status", status);
        map.put("createdAt", createdAt);
        return map;
    }
} 