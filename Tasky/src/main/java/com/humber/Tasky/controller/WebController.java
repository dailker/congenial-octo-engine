package com.humber.Tasky.controller;

import com.humber.Tasky.model.*;
import com.humber.Tasky.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final AuthService authService;
    private final TaskService taskService;
    private final UserService userService;
    private final TeamService teamService;
    private final TeamTaskService teamTaskService;

    @Autowired
    public WebController(AuthService authService, TaskService taskService, UserService userService, TeamService teamService, TeamTaskService teamTaskService) {
        this.taskService = taskService;
        this.authService = authService;
        this.userService = userService;
        this.teamService = teamService;
        this.teamTaskService = teamTaskService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal User user, Model model) {
        if (user != null) {
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
    public String showLoginPage(@AuthenticationPrincipal User user, Model model, CsrfToken csrfToken) {
        if (user != null) {
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
    public String listTasks(@AuthenticationPrincipal User user, Model model) {
        List<Task> tasks = taskService.getAllTasks(user);
        model.addAttribute("tasks", tasks);
        return "tasks";
    }

    @GetMapping("/teams")
    public String listUserTeams(@AuthenticationPrincipal User user, Model model) {
        List<Team> teams = teamService.getAllTeams(user);
        model.addAttribute("teams", teams);
        return "teams";
    }

    @GetMapping("/teams/new")
    public String showTeamForm(@AuthenticationPrincipal Team team, Model model) {
        return "team-form";
    }

    @GetMapping("/teams/edit/{id}")
    public String showEditTeamForm(@PathVariable String id, @AuthenticationPrincipal User user, Model model) {
        Team team = teamService.getTeamById(id);
        model.addAttribute("team", team);
        return "team-edit";
    }

    @GetMapping("/teams/{teamId}/tasks")
    public String listTeamTasks(@AuthenticationPrincipal User user, Model model, @PathVariable String teamId) {
        Team team = teamService.getTeamById(teamId);
        List<TeamTask> tasks = teamTaskService.getAllTasks(team);
        model.addAttribute("tasks", tasks);
        model.addAttribute("teamId", teamId);
        return "team-tasks";
    }
    @GetMapping("/teams/{teamId}/tasks/new")
    public String showTeamTaskForm(@AuthenticationPrincipal User user, Model model, @PathVariable String teamId) {
        Team team = teamService.getTeamById(teamId);
        model.addAttribute("teamId", teamId);
        return "team-task-form";
    }

    @GetMapping("/teams/{teamId}/tasks/edit/{id}")
    public String showEditTeamTaskForm(@PathVariable String id, @AuthenticationPrincipal User user, Model model, @PathVariable String teamId) {
        Task task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("teamId", teamId);
        return "team-task-edit";
    }

}