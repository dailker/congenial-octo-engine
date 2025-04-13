package com.humber.Tasky.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Document(collection = "friend_requests")
@CompoundIndexes({
    @CompoundIndex(name = "sender_recipient_idx", def = "{'senderId': 1, 'recipientId': 1}"),
    @CompoundIndex(name = "recipient_status_idx", def = "{'recipientId': 1, 'status': 1}"),
    @CompoundIndex(name = "sender_status_idx", def = "{'senderId': 1, 'status': 1}")
})
public class FriendRequest {
    @Id
    private String id;
    private String senderId;
    private String recipientId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }

    // Constructors
    public FriendRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public FriendRequest(String senderId, String recipientId) {
        this();
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.status = Status.PENDING;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}