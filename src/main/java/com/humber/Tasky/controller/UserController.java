package com.humber.Tasky.controller;

import com.humber.Tasky.model.FriendRequest;
import com.humber.Tasky.model.User;
import com.humber.Tasky.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/friend-request")
    public ResponseEntity<?> sendFriendRequest(@RequestParam String recipientEmail, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "You must be logged in to send a friend request."));
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid recipient email."));
        }

        try {
            userService.sendFriendRequest(currentUserEmail, recipientEmail);
            return ResponseEntity.ok(Map.of("message", "Friend request sent successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/friends/accept/{requestId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable String requestId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "You must be logged in to accept a friend request."));
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        try {
            User currentUser = userService.getUserByEmail(currentUserEmail);
            userService.acceptFriendRequest(currentUser.getId(), requestId);
            return ResponseEntity.ok(Map.of("message", "Friend request accepted successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage())); // Handle specific validation errors
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to accept friend request: " + e.getMessage()));
        }
    }

    @PostMapping("/friends/reject/{requestId}")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable String requestId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "You must be logged in to reject a friend request."));
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        try {
            User currentUser = userService.getUserByEmail(currentUserEmail);
            userService.rejectFriendRequest(currentUser.getId(), requestId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to reject friend request: " + e.getMessage()));
        }
    }

    @DeleteMapping("/friends/{friendId}")
    public void removeFriend(@PathVariable String friendId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("You must be logged in to remove a friend.");
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        userService.removeFriend(currentUserEmail, friendId);
    }

    @GetMapping("/friends/requests")
    public List<FriendRequest> getPendingFriendRequests(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("You must be logged in to view pending friend requests.");
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        return userService.getPendingFriendRequests(currentUserEmail);
    }

    @GetMapping("/friends/check/{userId}")
    public boolean areUsersConnected(@PathVariable String userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("You must be logged in to check friend connections.");
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        return userService.areUsersConnected(currentUserEmail, userId);
    }
}
