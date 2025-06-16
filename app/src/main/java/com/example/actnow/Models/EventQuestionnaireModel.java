package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EventQuestionnaireModel {
    private String questionnaireId;
    private String eventId;
    private boolean isEnabled;
    private List<Map<String, Object>> questions;
    private Timestamp createdAt;

    public EventQuestionnaireModel() {
        this.questions = new ArrayList<>();
        this.isEnabled = true;
        this.createdAt = Timestamp.now();
    }

    public EventQuestionnaireModel(String questionnaireId, String eventId) {
        this();
        this.questionnaireId = questionnaireId;
        this.eventId = eventId;
    }

    public String getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(String questionnaireId) { this.questionnaireId = questionnaireId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public List<Map<String, Object>> getQuestions() { return questions; }
    public void setQuestions(List<Map<String, Object>> questions) { this.questions = questions; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public void addQuestion(String questionId, String text, String type, List<String> options, boolean isRequired) {
        Map<String, Object> question = new HashMap<>();
        question.put("questionId", questionId);
        question.put("text", text);
        question.put("type", type);
        question.put("options", options != null ? options : new ArrayList<String>());
        question.put("isRequired", isRequired);
        questions.add(question);
    }

    public void removeQuestion(String questionId) {
        questions.removeIf(question -> questionId.equals(question.get("questionId")));
    }

    public void updateQuestion(String questionId, String text, String type, List<String> options, boolean isRequired) {
        for (Map<String, Object> question : questions) {
            if (questionId.equals(question.get("questionId"))) {
                question.put("text", text);
                question.put("type", type);
                question.put("options", options != null ? options : new ArrayList<String>());
                question.put("isRequired", isRequired);
                break;
            }
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("questionnaireId", questionnaireId);
        map.put("eventId", eventId);
        map.put("isEnabled", isEnabled);
        map.put("questions", questions);
        map.put("createdAt", createdAt);
        return map;
    }
} 