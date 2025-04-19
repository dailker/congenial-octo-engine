package com.humber.Tasky.controller;

import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.User;
import com.humber.Tasky.model.FriendRequest;
import com.humber.Tasky.model.FriendRequestWithUser;
import com.humber.Tasky.service.TeamService;
import com.humber.Tasky.service.UserService;

import com.humber.Tasky.repository.UserRepository;
import com.humber.Tasky.repository.ChatMessageRepository;
import com.humber.Tasky.repository.TeamRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private TeamRepository teamRepository;

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        return message;
    }

    @MessageMapping("/call")
    @SendTo("/topic/call")
    public String handleCall(String signalingData) {
        return signalingData;
    }

    @MessageMapping("/team/{teamId}/chat")
    @SendTo("/topic/team/{teamId}")
    public Team.ChatMessage handleTeamChatMessage(@DestinationVariable String teamId, Team.ChatMessage message) {
        if (teamId == null || teamId.isEmpty()) {
            logger.error("Team ID is missing or invalid.");
            throw new IllegalArgumentException("Team ID is missing or invalid.");
        }

        logger.info("Received message for team ID: {}", teamId);
        message.setTimestamp(LocalDateTime.now());

        // Save the message to the database
        Team.ChatMessage savedMessage = chatMessageRepository.save(message);

        // Add the message to the team's chat history
        teamRepository.findById(teamId).ifPresent(team -> {
            team.getChatMessages().add(savedMessage);
            teamRepository.save(team);
        });

        return savedMessage; // Broadcast the saved message
    }

    @MessageMapping("/team/{teamId}/chat-with-attachment")
    @SendTo("/topic/team/{teamId}")
    public Team.ChatMessage handleTeamChatMessageWithAttachment(@DestinationVariable String teamId, Team.ChatMessage message) {
        if (teamId == null || teamId.isEmpty()) {
            logger.error("Team ID is missing or invalid.");
            throw new IllegalArgumentException("Team ID is missing or invalid.");
        }

        logger.info("Received message with attachment for team ID: {}", teamId);
        message.setTimestamp(LocalDateTime.now());

        // Save the message to the database
        Team.ChatMessage savedMessage = chatMessageRepository.save(message);

        // Add the message to the team's chat history
        teamRepository.findById(teamId).ifPresent(team -> {
            team.getChatMessages().add(savedMessage);
            teamRepository.save(team);
        });

        return savedMessage; // Broadcast the saved message
    }

    @MessageMapping("/team/{teamId}/chat-with-file")
    @SendTo("/topic/team/{teamId}")
    public Team.ChatMessage handleTeamChatMessageWithFile(@DestinationVariable String teamId, Team.ChatMessage message) {
        if (teamId == null || teamId.isEmpty()) {
            logger.error("Team ID is missing or invalid.");
            throw new IllegalArgumentException("Team ID is missing or invalid.");
        }

        logger.info("Received message with file for team ID: {}", teamId);
        message.setTimestamp(LocalDateTime.now());

        // Save the message to the database
        Team.ChatMessage savedMessage = chatMessageRepository.save(message);

        // Add the message to the team's chat history
        teamRepository.findById(teamId).ifPresent(team -> {
            team.getChatMessages().add(savedMessage);
            teamRepository.save(team);
        });

        return savedMessage; // Broadcast the saved message
    }

    @MessageMapping("/team/{teamId}/update-members")
    @SendTo("/topic/team/{teamId}/members")
    public List<User> updateTeamMembers(@DestinationVariable String teamId, List<User> updatedMembers) {
        if (teamId == null || teamId.isEmpty()) {
            logger.error("Team ID is missing or invalid.");
            throw new IllegalArgumentException("Team ID is missing or invalid.");
        }
        logger.info("Broadcasting updated members for team ID: {}", teamId);
        return updatedMembers;
    }

    @MessageMapping("/team/{teamId}/list-members")
    @SendTo("/topic/team/{teamId}/members")
    public List<User> listTeamMembers(@DestinationVariable String teamId) {
        if (teamId == null || teamId.isEmpty()) {
            logger.error("Team ID is missing or invalid.");
            throw new IllegalArgumentException("Team ID is missing or invalid.");
        }
        logger.info("Fetching members for team ID: {}", teamId);
        return teamService.getTeamById(teamId)
                .map(team -> userRepository.findAllById(team.getMemberIds()))
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    @MessageMapping("/friend-requests/update")
    @SendTo("/topic/friend-requests")
    public List<FriendRequestWithUser> updateFriendRequests(String userId) {
        User user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<FriendRequest> receivedRequests = userService.getPendingFriendRequests(user.getId());
        return receivedRequests.stream()
                .map(request -> new FriendRequestWithUser(request, userService.getUserById(request.getSenderId()).orElse(null)))
                .collect(Collectors.toList());
    }

    @MessageMapping("/team-requests/update")
    @SendTo("/topic/team-requests")
    public List<Team> updateTeamRequests(String userId) {
        User user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return teamService.getTeamsForUser(user.getId());
    }
}
