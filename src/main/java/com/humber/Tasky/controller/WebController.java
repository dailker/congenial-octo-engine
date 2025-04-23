package com.humber.Tasky.controller;

import com.humber.Tasky.model.*;
import com.humber.Tasky.service.AuthService;
import com.humber.Tasky.service.TaskService;
import com.humber.Tasky.service.TeamService;
import com.humber.Tasky.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Objects;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    private final AuthService authService;
    private final TaskService taskService;
    private final UserService userService;
    private final TeamService teamService;

    @Autowired
    public WebController(AuthService authService, TaskService taskService, UserService userService, TeamService teamService) {
        this.taskService = taskService;
        this.authService = authService;
        this.userService = userService;
        this.teamService = teamService;
    }

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        if (principal != null) {
            User user = userService.getUserByEmail(principal.getName());
            List<Task> tasks = taskService.getAllTasks(user);
            model.addAttribute("tasks", tasks);
        }
        model.addAttribute("features", List.of(
            "Organize your tasks efficiently",
            "Share tasks with team members",
            "Calendar view for better planning",
            "Priority-based task management"
        ));
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage(Principal principal, Model model, CsrfToken csrfToken) {
        if (principal != null) {
            return "redirect:/tasks";
        }
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(@AuthenticationPrincipal User user, Model model, CsrfToken csrfToken) {
        if (user != null) {
            return "redirect:/tasks";
        }
        model.addAttribute("user", new User());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute User user, 
                                    Model model, 
                                    RedirectAttributes redirectAttributes) {
        try {
            authService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful!");
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/profile")
public String profile(Principal principal, Model model) {
    if (principal == null) {
        return "redirect:/login?error=not_authenticated";
    }

    try {
        User currentUser = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", currentUser);
        model.addAttribute("isCurrentUser", true);
        model.addAttribute("isConnected", false);
        model.addAttribute("hasPendingRequest", false);
        
        // Explicitly add requests (though @ModelAttribute should handle this)
        model.addAttribute("receivedRequests", userService.getPendingFriendRequests(currentUser.getId()));
        model.addAttribute("sentRequests", userService.getSentFriendRequests(currentUser.getId()));
        
        return "profile";
    } catch (Exception e) {
        System.out.println("Error fetching user profile: " + e.getMessage());
        return "redirect:/login?error=profile";
    }
}
    @GetMapping("/profile/{email}")
    public String viewProfile(@PathVariable String email, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login?error=not_authenticated";
        }

        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            User viewedUser = userService.getUserByEmail(email);

            model.addAttribute("user", viewedUser);
            model.addAttribute("isCurrentUser", currentUser.getId().equals(viewedUser.getId()));
            model.addAttribute("isConnected", userService.areUsersConnected(currentUser.getId(), viewedUser.getId()));
            model.addAttribute("hasPendingRequest", userService.hasPendingRequest(currentUser.getId(), viewedUser.getId()));

            return "profile";
        } catch (Exception e) {
            System.out.println("Error fetching user profile: " + e.getMessage());
            model.addAttribute("error", "Profile not found or inaccessible");
            return "profile";
        }
    }

    @GetMapping("/edit-profile")
    public String showEditProfileForm(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login?error=not_authenticated";
        }

        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            model.addAttribute("user", currentUser);
            return "edit-profile";
        } catch (Exception e) {
            System.out.println("Error fetching user profile: " + e.getMessage());
            return "redirect:/profile?error=profile_not_found";
        }
    }

    @PostMapping("/edit-profile")
    public String updateProfile(
            @RequestParam(required = false) String avatarUrl,
            @RequestParam String fullName,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        if (principal == null) {
            return "redirect:/login?error=not_authenticated";
        }

        if (fullName.length() > 50) {
            redirectAttributes.addFlashAttribute("error", "Full name cannot exceed 50 characters.");
            return "redirect:/edit-profile";
        }
        
        if (avatarUrl != null && !avatarUrl.isEmpty() && 
            !avatarUrl.endsWith(".jpg") && !avatarUrl.endsWith(".png")) {
            redirectAttributes.addFlashAttribute("error", "Only .jpg and .png files are allowed for avatar.");
            return "redirect:/edit-profile";
        }

        try {
            User currentUser = userService.getUserByEmail(principal.getName());
            
            User updates = new User();
            updates.setFullName(fullName);
            updates.setAvatarUrl(avatarUrl);
            
            userService.updateUser(currentUser.getId(), updates);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/edit-profile";
        }
    }

    @GetMapping("/tasks/new")
    public String showTaskForm(@AuthenticationPrincipal User user, Model model) {
        return "task-form";
    }

    @GetMapping("/tasks/edit/{id}")
    public String showEditTaskForm(@PathVariable String id, @AuthenticationPrincipal User user, Model model) {
        Task task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        return "task-edit";
    }

    @GetMapping("/tasks")
    public String listTasks(Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("error", "User not authenticated");
            return "tasks";
        }

        User user = userService.getUserByEmail(principal.getName());
        List<Task> tasks = taskService.getAllTasks(user);
        List<Task> acceptedTasks = taskService.getAcceptedTasks(user.getId());
        List<Task> editableTasks = taskService.getEditableTasks(user.getId());

        model.addAttribute("tasks", tasks);
        model.addAttribute("acceptedTasks", acceptedTasks);
        model.addAttribute("editableTasks", editableTasks);
        return "tasks";
    }

    @GetMapping("/online-friends")
    @ResponseBody
    public List<User> getOnlineFriends(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        User user = userService.getUserByEmail(principal.getName());
        return userService.getOnlineFriends(user.getId());
    }

    @GetMapping("/online-users")
    public String showOnlineUsersPage() {
        return "online-users";
    }

    @GetMapping("/teams")
    public String showTeamsPage() {
        return "teams";
    }

    @GetMapping("/create-team")
    public String showCreateTeamPage() {
        return "create-team";
    }

    @GetMapping("/teams/{id}")
    public String viewTeam(@PathVariable String id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?error=not_authenticated";
        }
    
        try {
            Optional<Team> team = teamService.getTeamById(id);
            if (team.isEmpty()) {
                model.addAttribute("error", "Team not found");
                return "error";
            }
    
            User currentUser = userService.getUserByEmail(principal.getName());
            boolean isOwner = "Owner".equals(team.get().getMemberPermissions().get(currentUser.getId()));
    
            List<User> members = team.get().getMemberIds().stream()
        .map(memberId -> userService.getUserById(memberId).orElse(null))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    Map<String, User> membersMap = members.stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));

        model.addAttribute("team", team.get());
        model.addAttribute("teamId", id);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("members", members);
        model.addAttribute("membersMap", membersMap); 
            return "team";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while loading the team");
            return "error";
        }
    }

    @PostMapping("/chat/send")
    @ResponseBody
    public void sendMessage(Principal principal, @RequestParam String recipientId, @RequestParam String message) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        User user = userService.getUserByEmail(principal.getName());
        userService.sendMessage(user.getId(), recipientId, message);
    }

    @GetMapping("/chat/messages/{friendId}")
    @ResponseBody
    public List<String> getChatMessages(Principal principal, @PathVariable String friendId) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        User user = userService.getUserByEmail(principal.getName());
        return userService.getChatMessages(user.getId(), friendId);
    }

    @PostMapping("/api/ping")
    @ResponseBody
    public void ping(Principal principal) {
        if (principal == null) {
            // Log the unauthenticated access attempt and return without throwing an exception
            logger.warn("Unauthenticated user attempted to ping.");
            return;
        }
        User user = userService.getUserByEmail(principal.getName());
        userService.updateUserOnlineStatus(user.getId(), true);
    }
}