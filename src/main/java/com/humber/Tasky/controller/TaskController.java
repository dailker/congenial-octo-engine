package com.humber.Tasky.controller;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.User;
import com.humber.Tasky.service.TaskService;
import com.humber.Tasky.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    @Autowired
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping
    public List<Task> getAllTasks(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        // Print tasks for debugging
        System.out.println(" - Tasks: " + taskService.getAllTasks(user));
        return taskService.getAllTasks(user);
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable String id, @RequestBody Task taskDetails) {
        return taskService.updateTask(id, taskDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable String id, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        try {
            taskService.deleteTask(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/complete/{taskId}")
    public Task completeTask(@PathVariable String taskId) {
        return taskService.completeTask(taskId);
    }
    
    @GetMapping("/completed")
    public List<Task> getCompletedTasks(
            @RequestParam boolean completed,
            Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getCompletedTasks(user, completed);
    }

    @GetMapping("/accepted")
    public List<Task> getAcceptedTasks(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getAcceptedTasks(user.getId());
    }

    @PostMapping("/uncomplete/{taskId}")
    public Task uncompleteTask(@PathVariable String taskId) {
        return taskService.uncompleteTask(taskId);
    }

    @PostMapping("/{taskId}/share")
    public Task shareTask(
            @PathVariable String taskId,
            @RequestParam String recipientEmail,
            Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.shareTask(taskId, user, userService.getUserByEmail(recipientEmail));
    }

    @PostMapping("/{taskId}/invite")
    public ResponseEntity<?> inviteUserToTask(@PathVariable String taskId, @RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        try {
            User user = userService.getUserByEmail(email);
            taskService.inviteUserToTask(taskId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Invitation sent successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{taskId}/accept")
    public ResponseEntity<?> acceptTaskInvitation(@PathVariable String taskId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        try {
            taskService.acceptTaskInvitation(taskId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Task invitation accepted successfully", "redirectUrl", "/tasks"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{taskId}/reject")
    public void rejectTaskInvitation(@PathVariable String taskId, @RequestParam String userId) {
        taskService.rejectTaskInvitation(taskId, userId);
    }

    @PostMapping("/{taskId}/leave")
    public ResponseEntity<?> leaveTask(@PathVariable String taskId, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        try {
            taskService.leaveTask(taskId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Successfully left the task"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{taskId}/leave-accepted")
    public ResponseEntity<?> leaveAcceptedTask(@PathVariable String taskId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not authenticated"));
        }

        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        try {
            taskService.leaveAcceptedTask(taskId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Successfully left the task"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/editable")
    public List<Task> getEditableTasks(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getEditableTasks(user.getId());
    }

    @GetMapping("/invitations")
    public List<Task> getTaskInvitations(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getTaskInvitations(user.getId());
    }

    @PostMapping("/{taskId}/invite-team")
    public ResponseEntity<?> inviteTeamToTask(@PathVariable String taskId, @RequestParam String teamId) {
        try {
            taskService.inviteTeamToTask(taskId, teamId);
            return ResponseEntity.ok(Map.of("message", "Team invited successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}