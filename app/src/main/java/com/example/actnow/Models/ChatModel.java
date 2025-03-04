package com.example.actnow.Models;

public class ChatModel {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String messageText;
    private long timestamp;
    private boolean seen;
    private String messageType;
    private String senderAvatarUrl; // Добавлено поле для аватара

    public ChatModel() {
        // Пустой конструктор для Firebase
    }

    // Измененный конструктор, чтобы учитывать URL аватара
    public ChatModel(String messageId, String senderId, String receiverId, String messageText,
                     long timestamp, boolean seen, String messageType, String senderAvatarUrl) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.seen = seen;
        this.messageType = messageType;
        this.senderAvatarUrl = senderAvatarUrl != null ? senderAvatarUrl : "";  // Убедимся, что аватар не null
    }

    // Геттеры и сеттеры
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getSenderAvatarUrl() { return senderAvatarUrl; } // Геттер для аватара
    public void setSenderAvatarUrl(String senderAvatarUrl) { this.senderAvatarUrl = senderAvatarUrl; } // Сеттер для аватара

    // Метод для получения времени сообщения в строковом формате
    public String getMessageTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(new java.util.Date(timestamp));
    }
}
