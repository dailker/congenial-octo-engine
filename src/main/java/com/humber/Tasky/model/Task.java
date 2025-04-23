package com.humber.Tasky.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private boolean completed;
    private Priority priority;
    private List<String> invitedUsers = new ArrayList<>();
    private List<Invitation> invitations = new ArrayList<>();
    
    @DBRef(lazy = true)
    @JsonBackReference("owner-tasks")
    private User owner;
    
    @DBRef(lazy = true)
    @JsonBackReference("creator-tasks")
    private User createdBy;

    public enum Priority { LOW, MEDIUM, HIGH }

    public static class Invitation {
        private String userId;
        private InvitationStatus status;

        public Invitation(String userId, InvitationStatus status) {
            this.userId = userId;
            this.status = status;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public InvitationStatus getStatus() {
            return status;
        }

        public void setStatus(InvitationStatus status) {
            this.status = status;
        }
    }

    public enum InvitationStatus {
        PENDING, ACCEPTED, REJECTED
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public List<String> getInvitedUsers() { return invitedUsers; }
    public void setInvitedUsers(List<String> invitedUsers) { this.invitedUsers = invitedUsers; }
    public List<Invitation> getInvitations() { return invitations; }
    public void setInvitations(List<Invitation> invitations) { this.invitations = invitations; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}