package com.humber.Tasky.controller;

import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.User;
import com.humber.Tasky.repository.UserRepository;
import com.humber.Tasky.service.TeamService;
import com.humber.Tasky.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RestController
@Tag(name = "Team API")
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    private final TeamService teamService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final UserRepository userRepository;

    public TeamController(TeamService teamService, UserService userService, UserRepository userRepository) {
        this.teamService = teamService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable String id) {
        Optional<Team> team = teamService.getTeamById(id);
        if (team.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Team not found"));
        }
        return ResponseEntity.ok(team.get());
    }

    @Operation(summary = "Get all teams", description = "Retrieve all teams for the authenticated user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping
    public ResponseEntity<?> getAllTeams(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);
            List<Team> teams = teamService.getTeamsForUser(user.getId());
            return ResponseEntity.ok(teams);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Create a team", description = "Create a new team")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Team team, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);

            // Add the creator as a member and set them as the owner
            team.getMemberIds().add(user.getId());
            team.getMemberPermissions().put(user.getId(), "Owner");

            Team createdTeam = teamService.createTeam(team);
            return ResponseEntity.ok(createdTeam);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Add a member to a team", description = "Add a member to a team by their ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{teamId}/add-member/{userId}")
    public ResponseEntity<?> addMemberToTeam(@PathVariable String teamId, @PathVariable String userId) {
        if (teamId == null || teamId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Team ID is required"));
        }
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User ID is required"));
        }

        try {
            teamService.addMemberToTeam(teamId, userId);
            return ResponseEntity.ok(Map.of("message", "Member added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Remove a member from a team", description = "Remove a member from a team by their ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @DeleteMapping("/{teamId}/remove-member/{userId}")
    public ResponseEntity<?> removeMemberFromTeam(@PathVariable String teamId, @PathVariable String userId,
                                                  @RequestParam String requesterId) {
        if (teamId == null || teamId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Team ID is required"));
        }
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User ID is required"));
        }
        if (requesterId == null || requesterId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Requester ID is required"));
        }

        try {
            teamService.removeMemberFromTeam(teamId, userId, requesterId);
            return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Invite a user to a team", description = "Invite a user to a team by their email")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User invited successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{teamId}/invite")
    public ResponseEntity<?> inviteUserToTeam(@PathVariable String teamId, @RequestBody Map<String, String> requestBody, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        String email = requestBody.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        try {
            String currentUserEmail = principal.getName();
            User currentUser = userService.getUserByEmail(currentUserEmail);

            Team team = teamService.getTeamById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));

            // Prevent inviting existing members or already invited users
            if (team.getMemberIds().contains(currentUser.getId()) || team.getInvitations().contains(email)) {
                return ResponseEntity.badRequest().body(Map.of("message", "User is already a member or has been invited"));
            }

            teamService.inviteUserToTeam(teamId, email);
            return ResponseEntity.ok(Map.of("message", "User invited successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Send a message to a team", description = "Send a message to a team chat")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{teamId}/send-message")
    public ResponseEntity<?> sendMessageToTeam(@PathVariable String teamId, @RequestParam String senderId,
                                               @RequestParam String message, @RequestParam(required = false) String fileUrl) {
        if (teamId == null || teamId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Team ID is required"));
        }
        if (senderId == null || senderId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sender ID is required"));
        }
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message content is required"));
        }

        try {
            teamService.sendMessageToTeam(teamId, senderId, message, fileUrl);
            return ResponseEntity.ok(Map.of("message", "Message sent successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Send a join request to a team", description = "Send a join request to a team")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Join request sent successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{teamId}/join-request")
    public ResponseEntity<?> sendJoinRequest(@PathVariable String teamId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);
            teamService.sendJoinRequest(teamId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Join request sent successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Leave a team", description = "Leave a team")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Left team successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{teamId}/leave")
    public ResponseEntity<?> leaveTeam(@PathVariable String teamId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);
            teamService.leaveTeam(teamId, user.getId());
            return ResponseEntity.ok(Map.of("message", "You have left the team"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Transfer ownership of a team", description = "Transfer ownership of a team to another member")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{teamId}/transfer-ownership")
    public ResponseEntity<?> transferOwnership(@PathVariable String teamId, @RequestBody Map<String, String> requestBody, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }
        if (teamId == null || teamId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Team ID is required"));
        }
        String newOwnerId = requestBody.get("newOwnerId");
        if (newOwnerId == null || newOwnerId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "New owner ID is required"));
        }

        try {
            String currentOwnerEmail = principal.getName();
            User currentOwner = userService.getUserByEmail(currentOwnerEmail);
            teamService.transferOwnership(teamId, currentOwner.getId(), newOwnerId);
            return ResponseEntity.ok(Map.of("message", "Ownership transferred successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Delete a team", description = "Delete a team")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @DeleteMapping("/{teamId}")
    public ResponseEntity<?> deleteTeam(@PathVariable String teamId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);
            teamService.deleteTeam(teamId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Team deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "List team members", description = "List all members of a team")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Members listed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/{id}/listMembers")
public ResponseEntity<?> listTeamMembers(@PathVariable String id) {
    Optional<Team> team = teamService.getTeamById(id);
    if (team.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Team not found"));
    }

    List<String> memberIds = team.get().getMemberIds();
    List<User> members = userRepository.findAllById(memberIds); // Fetch all users in a single query

    // Create a list of user info with full name
    List<Map<String, String>> memberInfo = members.stream()
        .map(user -> Map.of(
            "id", user.getId(),
            "fullName", user.getFullName() // Assuming you have a getFullName() method in User
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok(memberInfo);
}

    @Operation(summary = "Get chat messages", description = "Retrieve chat messages for a team")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Chat messages retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/{teamId}/chat-messages")
    public ResponseEntity<?> getChatMessages(@PathVariable String teamId) {
        if (teamId == null || teamId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Team ID is required"));
        }

        Optional<Team> team = teamService.getTeamById(teamId);
        if (team.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Team not found"));
        }

        return ResponseEntity.ok(team.get().getChatMessages());
    }
}
