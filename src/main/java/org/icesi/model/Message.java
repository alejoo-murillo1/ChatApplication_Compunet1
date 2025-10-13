package org.icesi.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private final int senderId;
    private final int recipientId;
    private final String content;
    private final LocalDateTime timestamp;
    private final boolean isGroup;

    public Message(int senderId, int recipientId, String content, boolean isGroup) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.isGroup = isGroup;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isGroup() {
        return isGroup;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + content;
    }
}
