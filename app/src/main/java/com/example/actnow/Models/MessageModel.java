package com.example.actnow.Models;

public class MessageModel {
    private String messageId;
    private String chatId;
    private String senderId;
    private String content;
    private String imageUrl;
    private long timestamp;

    // Пустой конструктор для Firestore
    public MessageModel() {}

    public MessageModel(String messageId, String chatId, String senderId, String content, String imageUrl, long timestamp) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
