package com.humber.Tasky.controller;

import com.humber.Tasky.model.FriendRequest;
import com.humber.Tasky.model.User;
import com.humber.Tasky.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "User", description = "User management APIs")
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Send a friend request", description = "Send a friend request to another user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
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

    @Operation(summary = "Accept a friend request", description = "Accept a friend request from another user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Friend request accepted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
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

    @Operation(summary = "Reject a friend request", description = "Reject a friend request from another user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Friend request rejected successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
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

    @Operation(summary = "Remove a friend", description = "Remove a friend from the user's friend list")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Friend removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @DeleteMapping("/friends/{friendId}")
    public void removeFriend(@PathVariable String friendId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("You must be logged in to remove a friend.");
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        userService.removeFriend(currentUserEmail, friendId);
    }

    @Operation(summary = "Get pending friend requests", description = "Retrieve a list of pending friend requests for the user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending friend requests retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/friends/requests")
    public List<FriendRequest> getPendingFriendRequests(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("You must be logged in to view pending friend requests.");
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        return userService.getPendingFriendRequests(currentUserEmail);
    }

    @Operation(summary = "Check if users are connected", description = "Check if two users are connected as friends")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users are connected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/friends/check/{userId}")
    public boolean areUsersConnected(@PathVariable String userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("You must be logged in to check friend connections.");
        }

        String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
        return userService.areUsersConnected(currentUserEmail, userId);
    }

    @Operation(summary = "Get online friends", description = "Retrieve a list of online friends for the user")
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Online friends retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
})
@GetMapping("/onlineUsers")
public ResponseEntity<List<Map<String, Object>>> getOnlineUsers(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of(Map.of("message", "User not authenticated")));
    }

    String currentUserEmail = authentication.getName(); // Retrieve the authenticated user's email
    User currentUser = userService.getUserByEmail(currentUserEmail); // Get the current user based on email

    List<User> onlineUsers = userService.getOnlineUsers();
    
    // Prepare the response with the 'currentUser' flag
    List<Map<String, Object>> onlineUsersWithCurrentFlag = onlineUsers.stream().map(user -> {
        Map<String, Object> userMap = Map.of(
            "id", user.getId(),
            "fullName", user.getFullName(),
            "avatarUrl", user.getAvatarUrl(),
            "email", user.getEmail(),
            "online", user.isOnline(),
            "currentUser", user.getId().equals(currentUser.getId()) // Add currentUser flag
        );
        return userMap;
    }).collect(Collectors.toList());

    return ResponseEntity.ok(onlineUsersWithCurrentFlag);
}

}
