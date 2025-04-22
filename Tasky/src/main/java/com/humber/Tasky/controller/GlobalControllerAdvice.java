package com.humber.Tasky.controller;

import com.humber.Tasky.model.FriendRequest;
import com.humber.Tasky.model.FriendRequestWithUser;
import com.humber.Tasky.model.User;
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

    public GlobalControllerAdvice(UserService userService) {
        this.userService = userService;
    }
    @ModelAttribute
    public void addFriendRequests(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            try {
                String email = auth.getName();
                User user = userService.getUserByEmail(email);
                
                // Get requests with user data
                List<FriendRequest> receivedRequests = userService.getPendingFriendRequests(user.getId());
                List<FriendRequest> sentRequests = userService.getSentFriendRequests(user.getId());
                
                // Create DTOs with user data
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
                
                // Add to model
                model.addAttribute("receivedWithSenders", receivedWithSenders);
                model.addAttribute("sentWithRecipients", sentWithRecipients);
                
            } catch (Exception e) {
                logger.error("Error loading friend requests", e);
            }
        }
    }
}