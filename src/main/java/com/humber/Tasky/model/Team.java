package com.humber.Tasky.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@Document(collection = "teams")
public class Team {
    @Id
    private String id;

    private String name;

    private List<String> memberIds = new ArrayList<>(); // Initialize the list
    private List<String> invitations = new ArrayList<>(); // Store email invitations
    private Map<String, String> memberPermissions = new HashMap<>(); // Map userId to permission (Owner/User)
    @DBRef(lazy = true)
    private List<ChatMessage> chatMessages = new ArrayList<>(); // Use DBRef for chat messages

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }
    public List<String> getInvitations() { return invitations; }
    public void setInvitations(List<String> invitations) { this.invitations = invitations; }
    public Map<String, String> getMemberPermissions() { return memberPermissions; }
    public void setMemberPermissions(Map<String, String> memberPermissions) { this.memberPermissions = memberPermissions; }
    public List<ChatMessage> getChatMessages() { return chatMessages; }
    public void setChatMessages(List<ChatMessage> chatMessages) { this.chatMessages = chatMessages; }

    public static class ChatMessage {
        @Id
        private String id;
        private String senderId;
        private String message;
        private String fileUrl; // Optional file URL
        private LocalDateTime timestamp;

        public ChatMessage(String senderId, String message, String fileUrl) {
            this.senderId = senderId;
            this.message = message;
            this.fileUrl = fileUrl;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
