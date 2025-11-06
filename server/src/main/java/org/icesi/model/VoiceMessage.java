package org.icesi.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class VoiceMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private final int senderId;
    private final int recipientId;
    private final byte[] audioData;
    private final LocalDateTime timestamp;
    private final boolean isGroup;
    private final String senderUsername;

    public VoiceMessage(int senderId, int recipientId, byte[] audioData,
                        boolean isGroup, String senderUsername) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.audioData = audioData;
        this.isGroup = isGroup;
        this.timestamp = LocalDateTime.now();
        this.senderUsername = senderUsername;
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

    public byte[] getAudioData() {
        return audioData;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] Mensaje de voz de " + senderUsername;
    }
}
