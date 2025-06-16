package com.example.actnow.Models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MessageModel {
    private String messageId;
    private String chatId;
    private String senderId;
    private String receiverId;
    private String content;
    private String type;
    private List<String> images;
    private String fileUrl;
    private String fileName;
    private long fileSize;
    private List<String> mentionedUsers;
    private boolean isRead; // Поле
    private Timestamp createdAt;

    public MessageModel() {
        this.images = new ArrayList<>();
        this.mentionedUsers = new ArrayList<>();
        this.isRead = false;
        this.createdAt = Timestamp.now();
    }

    public MessageModel(String messageId, String chatId, String senderId, String receiverId, String content, String type) {
        this();
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public List<String> getMentionedUsers() { return mentionedUsers; }
    public void setMentionedUsers(List<String> mentionedUsers) { this.mentionedUsers = mentionedUsers; }

    public boolean isRead() { return isRead; } // Геттер для булевого поля
    public void setIsRead(boolean isRead) { this.isRead = isRead; } // Сеттер с правильным именем

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("chatId", chatId);
        map.put("senderId", senderId);
        map.put("receiverId", receiverId);
        map.put("content", content);
        map.put("type", type);
        map.put("images", images);
        map.put("fileUrl", fileUrl);
        map.put("fileName", fileName);
        map.put("fileSize", fileSize);
        map.put("mentionedUsers", mentionedUsers);
        map.put("isRead", isRead);
        map.put("createdAt", createdAt);
        return map;
    }

    public void addImage(String imageUrl) {
        if (images.size() < 5 && !images.contains(imageUrl)) {
            images.add(imageUrl);
        }
    }

    public void removeImage(String imageUrl) {
        images.remove(imageUrl);
    }

    public void addMentionedUser(String userId) {
        if (!mentionedUsers.contains(userId)) {
            mentionedUsers.add(userId);
        }
    }

    public void removeMentionedUser(String userId) {
        mentionedUsers.remove(userId);
    }
}