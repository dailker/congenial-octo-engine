package com.humber.Tasky.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.*;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
    
    private String avatarUrl;
    private List<String> invitations = new ArrayList<>();
    private List<String> friends = new ArrayList<>();
    private List<String> sentFriendRequests = new ArrayList<>();
    private List<String> receivedFriendRequests = new ArrayList<>();
    
    @DBRef(lazy = true)
    @JsonManagedReference("owner-tasks")
    private Set<Task> tasks = new HashSet<>();

    private boolean online; // Add this field to track online status

    private List<Task.Invitation> taskInvitations = new ArrayList<>();

    public User() {}
    
    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public List<String> getInvitations() { return invitations; }
    public List<String> getFriends() { return friends; }
    public List<String> getSentFriendRequests() { return sentFriendRequests; }
    public List<String> getReceivedFriendRequests() { return receivedFriendRequests; }
    public Set<Task> getTasks() { return tasks; }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public List<Task.Invitation> getTaskInvitations() {
        return taskInvitations;
    }

    public void setTaskInvitations(List<Task.Invitation> taskInvitations) {
        this.taskInvitations = taskInvitations;
    }

    // Business methods
    public void addInvitation(String inviterId) {
        if (!invitations.contains(inviterId)) {
            invitations.add(inviterId);
        }
    }

    public void removeInvitation(String inviterId) {
        invitations.remove(inviterId);
    }

    public void addFriend(String friendId) {
        if (!friends.contains(friendId)) {
            friends.add(friendId);
        }
    }

    public void removeFriend(String friendId) {
        friends.remove(friendId);
    }

    public void addSentFriendRequest(String requestId) {
        if (!sentFriendRequests.contains(requestId)) {
            sentFriendRequests.add(requestId);
        }
    }

    public void removeSentFriendRequest(String requestId) {
        sentFriendRequests.remove(requestId);
    }

    public void addReceivedFriendRequest(String requestId) {
        if (!receivedFriendRequests.contains(requestId)) {
            receivedFriendRequests.add(requestId);
        }
    }

    public void removeReceivedFriendRequest(String requestId) {
        receivedFriendRequests.remove(requestId);
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}