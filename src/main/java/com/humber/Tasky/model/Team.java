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

    private List<String> memberIds = new ArrayList<>();
    private List<String> invitations = new ArrayList<>();
    private Map<String, String> memberPermissions = new HashMap<>();
    @DBRef(lazy = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();

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
        private String fileId;  // Changed from fileUrl to fileId to match template
        private String fileName; // Added to store original filename
        private String fileType; // Added to store file type
        private LocalDateTime timestamp;

        // Default constructor
        public ChatMessage() {
            this.timestamp = LocalDateTime.now();
        }

        // Constructor for text messages
        public ChatMessage(String senderId, String message) {
            this();
            this.senderId = senderId;
            this.message = message;
        }

        // Constructor for file messages
        public ChatMessage(String senderId, String message, String fileId, String fileName, String fileType) {
            this(senderId, message);
            this.fileId = fileId;
            this.fileName = fileName;
            this.fileType = fileType;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getFileId() { return fileId; }
        public void setFileId(String fileId) { this.fileId = fileId; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        // Helper method to check if message has file
        public boolean hasFile() {
            return fileId != null && !fileId.isEmpty();
        }
    }
}