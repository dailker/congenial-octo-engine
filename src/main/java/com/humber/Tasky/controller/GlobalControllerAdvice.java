package com.humber.Tasky.controller;

import com.humber.Tasky.model.FriendRequest;
import com.humber.Tasky.model.FriendRequestWithUser;
import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.User;
import com.humber.Tasky.repository.TaskRepository;
import com.humber.Tasky.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    private final UserService userService;
    private final TaskRepository taskRepository;

    public GlobalControllerAdvice(UserService userService, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @ModelAttribute
    public void addFriendRequests(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            try {
                String email = auth.getName();
                User user = userService.getUserByEmail(email);

                List<FriendRequest> receivedRequests = userService.getPendingFriendRequests(user.getId());
                List<FriendRequest> sentRequests = userService.getSentFriendRequests(user.getId());

                List<FriendRequestWithUser> receivedWithSenders = receivedRequests.stream()
                    .map(request -> {
                        User sender = userService.getUserById(request.getSenderId()).orElse(null);
                        return new FriendRequestWithUser(request, sender);
                    })
                    .collect(Collectors.toList());

                List<FriendRequestWithUser> sentWithRecipients = sentRequests.stream()
                    .map(request -> {
                        User recipient = userService.getUserById(request.getRecipientId()).orElse(null);
                        return new FriendRequestWithUser(request, recipient);
                    })
                    .collect(Collectors.toList());

                model.addAttribute("receivedWithSenders", receivedWithSenders);
                model.addAttribute("sentWithRecipients", sentWithRecipients);
            } catch (Exception e) {
                logger.error("Error loading friend requests", e);
                model.addAttribute("receivedWithSenders", List.of()); // Add empty list on error
            }
        } else {
            model.addAttribute("receivedWithSenders", List.of()); // Add empty list if not authenticated
        }
    }

    @ModelAttribute
    public void addTaskInvitations(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            try {
                String email = auth.getName();
                User user = userService.getUserByEmail(email);

                List<Task> taskInvitations = taskRepository.findByInvitedUsersContaining(user.getId());
                model.addAttribute("taskInvitations", taskInvitations);
            } catch (Exception e) {
                logger.error("Error loading task invitations", e);
                model.addAttribute("taskInvitations", List.of()); // Add empty list on error
            }
        } else {
            model.addAttribute("taskInvitations", List.of()); // Add empty list if not authenticated
        }
    }
}