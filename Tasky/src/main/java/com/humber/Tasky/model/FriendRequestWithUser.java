package com.humber.Tasky.model;

public class FriendRequestWithUser {
    private FriendRequest request;
    private User user;
    
    public FriendRequestWithUser(FriendRequest request, User user) {
        this.request = request;
        this.user = user;
    }
    
    // Getters
    public FriendRequest getRequest() { return request; }
    public User getUser() { return user; }
}