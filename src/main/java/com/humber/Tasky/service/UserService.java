package com.humber.Tasky.service;

import com.humber.Tasky.model.*;
import com.humber.Tasky.repository.FriendRequestRepository;
import com.humber.Tasky.repository.UserRepository;
import com.humber.Tasky.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, 
                     FriendRequestRepository friendRequestRepository,
                     MessageRepository messageRepository,
                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // User CRUD operations
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User updateUser(String id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }
        if (userDetails.getAvatarUrl() != null) {
            user.setAvatarUrl(userDetails.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    // Friend request operations
    public boolean areUsersConnected(String userId1, String userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user1.getFriends().contains(userId2);
    }

    public FriendRequest sendFriendRequest(String senderEmail, String recipientEmail) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new RuntimeException("Recipient email cannot be null or empty");
        }

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new RuntimeException("Recipient with email " + recipientEmail + " not found"));

        if (sender.getId().equals(recipient.getId())) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        if (areUsersConnected(sender.getId(), recipient.getId())) {
            throw new RuntimeException("Users are already friends");
        }

        Optional<FriendRequest> existingRequest = friendRequestRepository
                .findBySenderIdAndRecipientId(sender.getId(), recipient.getId());

        if (existingRequest.isPresent()) {
            throw new RuntimeException("Friend request already sent");
        }

        FriendRequest friendRequest = new FriendRequest(sender.getId(), recipient.getId());
        friendRequestRepository.save(friendRequest);

        sender.addSentFriendRequest(friendRequest.getId());
        recipient.addReceivedFriendRequest(friendRequest.getId());
        userRepository.save(sender);
        userRepository.save(recipient);

        return friendRequest;
    }

    public void acceptFriendRequest(String userId, String requestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendRequest.getRecipientId().equals(userId)) {
            throw new RuntimeException("Not authorized - you must be the sender or recipient to accept this friend request");
        }

        if (friendRequest.getStatus() != FriendRequest.Status.PENDING) {
            throw new RuntimeException("Friend request is not pending");
        }

        friendRequest.setStatus(FriendRequest.Status.ACCEPTED);
        friendRequestRepository.save(friendRequest);

        User sender = userRepository.findById(friendRequest.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User recipient = userRepository.findById(friendRequest.getRecipientId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        sender.addFriend(recipient.getId());
        recipient.addFriend(sender.getId());
        
        sender.removeSentFriendRequest(requestId);
        recipient.removeReceivedFriendRequest(requestId);
        
        userRepository.save(sender);
        userRepository.save(recipient);
        
        System.out.println(LocalDateTime.now() + " - Friend request accepted between " + 
                         sender.getEmail() + " and " + recipient.getEmail());
    }

    public void rejectFriendRequest(String userId, String requestId) {
        logger.info("Rejecting friend request: userId={}, requestId={}", userId, requestId);
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendRequest.getRecipientId().equals(userId)) {
            logger.error("Unauthorized rejection attempt: userId={}, requestId={}", userId, requestId);
            throw new RuntimeException("Not authorized - you must be the sender or recipient to reject this friend request");
        }

        friendRequest.setStatus(FriendRequest.Status.REJECTED);
        friendRequestRepository.save(friendRequest);

        User sender = userRepository.findById(friendRequest.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User recipient = userRepository.findById(friendRequest.getRecipientId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        sender.removeSentFriendRequest(requestId);
        recipient.removeReceivedFriendRequest(requestId);

        userRepository.save(sender);
        userRepository.save(recipient);

        logger.info("Friend request rejected successfully: userId={}, requestId={}", userId, requestId);
    }

    public void removeFriend(String userId, String friendId) {
        logger.info("Removing friend: userId={}, friendId={}", userId, friendId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        user.removeFriend(friendId);
        friend.removeFriend(userId);

        // Remove any pending friend requests between these users
        Optional<FriendRequest> requests = friendRequestRepository
                .findBySenderIdAndRecipientId(userId, friendId);
        if (requests.isPresent()) {
            
            requests.ifPresent(request -> {
                user.removeSentFriendRequest(request.getId());
                user.removeReceivedFriendRequest(request.getId());
                friend.removeSentFriendRequest(request.getId());
                friend.removeReceivedFriendRequest(request.getId());
                friendRequestRepository.delete(request);
            });
    
            userRepository.save(user);
            userRepository.save(friend);
    
            
        }
    }

    public List<FriendRequest> getPendingFriendRequests(String userId) {
        return friendRequestRepository.findByRecipientIdAndStatus(userId, FriendRequest.Status.PENDING);
    }

    public List<FriendRequest> getSentFriendRequests(String userId) {
        return friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequest.Status.PENDING);
    }
    

    public boolean hasPendingRequest(String senderId, String recipientId) {
        return friendRequestRepository.findBySenderIdAndRecipientIdAndStatus(
                senderId, recipientId, FriendRequest.Status.PENDING).isPresent() ||
               friendRequestRepository.findBySenderIdAndRecipientIdAndStatus(
                recipientId, senderId, FriendRequest.Status.PENDING).isPresent();
    }

    // Authentication and registration
    public User registerUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean validateCredentials(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    // Other user management methods
    public void updatePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateUserEmail(String userId, String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new RuntimeException("Email cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already in use");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public void updateUserFullName(String userId, String newFullName) {
        if (newFullName == null || newFullName.trim().isEmpty()) {
            throw new RuntimeException("Full name cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(newFullName);
        userRepository.save(user);
    }

    public void updateUserAvatar(String userId, String newAvatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);
    }

    public void updateUserOnlineStatus(String userId, boolean isOnline) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setOnline(isOnline);
        userRepository.save(user);
    }

    // New methods for online friends and messaging
    public List<User> getOnlineFriends(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Assuming a method exists to fetch online friends
        return user.getFriends().stream()
                .map(friendId -> userRepository.findById(friendId).orElse(null))
                .filter(Objects::nonNull)
                .filter(User::isOnline) // Assuming User has an isOnline method
                .collect(Collectors.toList());
    }

    public List<User> getOnlineUsers() {
        return userRepository.findByOnline(true);
    }

    public void sendMessage(String senderId, String recipientId, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty");
        }

        if (!userRepository.existsById(senderId)) {
            throw new RuntimeException("Sender not found");
        }

        if (!userRepository.existsById(recipientId)) {
            throw new RuntimeException("Recipient not found");
        }

        Message newMessage = new Message(senderId, recipientId, message, LocalDateTime.now());
        messageRepository.save(newMessage);
    }

    public List<String> getChatMessages(String userId, String friendId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        if (!userRepository.existsById(friendId)) {
            throw new RuntimeException("Friend not found");
        }

        return messageRepository.findBySenderIdAndRecipientId(userId, friendId).stream()
                .map(Message::getContent)
                .collect(Collectors.toList());
    }
}