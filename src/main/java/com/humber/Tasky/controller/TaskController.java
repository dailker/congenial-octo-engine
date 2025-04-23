package com.humber.Tasky.controller;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.User;
import com.humber.Tasky.service.TaskService;
import com.humber.Tasky.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Task", description = "Task management APIs")
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    @Autowired
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @Operation(summary = "Get all tasks", description = "Retrieve all tasks for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping
    public List<Task> getAllTasks(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getAllTasks(user);
    }

    @Operation(summary = "Create a new task", description = "Create and return a new task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid task input")
    })
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    @Operation(summary = "Update a task", description = "Update a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable String id, @RequestBody Task taskDetails) {
        return taskService.updateTask(id, taskDetails);
    }

    @Operation(summary = "Delete a task", description = "Delete a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Task deletion failed")
    })
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

    @Operation(summary = "Complete a task", description = "Mark a task as completed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task completed successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/complete/{taskId}")
    public Task completeTask(@PathVariable String taskId) {
        return taskService.completeTask(taskId);
    }

    @Operation(summary = "Get tasks by completion status", description = "Retrieve tasks based on their completion status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping("/completed")
    public List<Task> getCompletedTasks(@RequestParam boolean completed, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getCompletedTasks(user, completed);
    }

    @Operation(summary = "Get accepted tasks", description = "Retrieve tasks accepted by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accepted tasks retrieved successfully")
    })
    @GetMapping("/accepted")
    public List<Task> getAcceptedTasks(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getAcceptedTasks(user.getId());
    }

    @Operation(summary = "Uncomplete a task", description = "Mark a task as not completed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task marked as not completed")
    })
    @PostMapping("/uncomplete/{taskId}")
    public Task uncompleteTask(@PathVariable String taskId) {
        return taskService.uncompleteTask(taskId);
    }

    @Operation(summary = "Share a task", description = "Share a task with another user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task shared successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to share task")
    })
    @PostMapping("/{taskId}/share")
    public Task shareTask(@PathVariable String taskId, @RequestParam String recipientEmail, Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.shareTask(taskId, user, userService.getUserByEmail(recipientEmail));
    }

    @Operation(summary = "Invite user to task", description = "Invite a user to collaborate on a task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email or user not found")
    })
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

    @Operation(summary = "Accept task invitation", description = "Accept a task invitation by task ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task invitation accepted successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "400", description = "Task invitation acceptance failed")
    })
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

    @Operation(summary = "Reject task invitation", description = "Reject a task invitation by task ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task invitation rejected successfully")
    })
    @PostMapping("/{taskId}/reject")
    public void rejectTaskInvitation(@PathVariable String taskId, @RequestParam String userId) {
        taskService.rejectTaskInvitation(taskId, userId);
    }

    @Operation(summary = "Leave a task", description = "Leave a task the user is participating in")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully left the task"),
            @ApiResponse(responseCode = "400", description = "Failed to leave the task")
    })
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

    @Operation(summary = "Leave accepted task", description = "Leave a task the user has accepted")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully left the accepted task"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "400", description = "Failed to leave the task")
    })
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

    @Operation(summary = "Get editable tasks", description = "Retrieve tasks the user can edit")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Editable tasks retrieved successfully")
    })
    @GetMapping("/editable")
    public List<Task> getEditableTasks(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getEditableTasks(user.getId());
    }

    @Operation(summary = "Get task invitations", description = "Retrieve task invitations for the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task invitations retrieved successfully")
    })
    @GetMapping("/invitations")
    public List<Task> getTaskInvitations(Principal principal) {
        String email = principal.getName();
        User user = userService.getUserByEmail(email);
        return taskService.getTaskInvitations(user.getId());
    }

    @Operation(summary = "Invite team to task", description = "Invite an entire team to collaborate on a task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Team invited successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to invite team")
    })
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
