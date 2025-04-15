package com.humber.Tasky.service;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.User;
import com.humber.Tasky.model.Task.Priority;
import com.humber.Tasky.repository.TaskRepository;
import com.humber.Tasky.repository.TeamRepository;
import com.humber.Tasky.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    public List<Task> getAllTasks(User owner) {
        if (owner == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        return taskRepository.findByOwner(owner);
    }

    public Task completeTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        task.setCompleted(true);
        return taskRepository.save(task);
    }
    public Task uncompleteTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    
        task.setCompleted(false);
        
        return taskRepository.save(task);
    }

    public Task createTask(Task task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User must be authenticated to create a task");
        }
        
        String email = authentication.getName();
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        task.setCreatedBy(createdBy);
        task.setOwner(createdBy);
        Task savedTask = taskRepository.save(task);
        
        createdBy.addTask(savedTask);
        userRepository.save(createdBy);
        
        return savedTask;
    }

    public Task updateTask(String id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDueDate(taskDetails.getDueDate());
        task.setCompleted(taskDetails.isCompleted());
        task.setPriority(taskDetails.getPriority());
        
        return taskRepository.save(task);
    }

    public void deleteTask(String id, String userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Check if the user is the owner of the task
        if (!task.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only the owner can delete the task");
        }

        User owner = task.getOwner();
        if (owner != null) {
            owner.getTasks().remove(task);
            userRepository.save(owner);
        }

        taskRepository.delete(task);
    }

    public List<Task> getTasksByDateRange(User owner, LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByOwnerAndDueDateBetween(owner, start, end);
    }

    public List<Task> getCompletedTasks(User owner, boolean completed) {
        return taskRepository.findByOwnerAndCompleted(owner, completed);
    }

    public Task shareTask(String taskId, User owner, User newOwner) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getOwner().equals(owner)) {
            throw new RuntimeException("You don't have permission to share this task");
        }

        Task sharedTask = new Task();
        sharedTask.setTitle(task.getTitle());
        sharedTask.setDescription(task.getDescription());
        sharedTask.setDueDate(task.getDueDate());
        sharedTask.setPriority(task.getPriority());
        sharedTask.setOwner(newOwner);
        sharedTask.setCreatedBy(owner);

        Task savedTask = taskRepository.save(sharedTask);
        newOwner.addTask(savedTask);
        userRepository.save(newOwner);
        
        return savedTask;
    }

    public void inviteUserToTask(String taskId, String userIdOrEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userIdOrEmail)
                .orElseGet(() -> userRepository.findByEmail(userIdOrEmail)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        boolean alreadyInvited = task.getInvitations().stream()
                .anyMatch(inv -> inv.getUserId().equals(user.getId()) && inv.getStatus() == Task.InvitationStatus.PENDING);

        if (!alreadyInvited) {
            task.getInvitations().add(new Task.Invitation(user.getId(), Task.InvitationStatus.PENDING));
            user.getTaskInvitations().add(new Task.Invitation(task.getId(), Task.InvitationStatus.PENDING));
            taskRepository.save(task);
            userRepository.save(user);
        }
    }
    
    public void inviteTeamToTask(String taskId, String teamId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        team.getMemberIds().forEach(memberId -> {
            if (!task.getInvitedUsers().contains(memberId)) {
                task.getInvitedUsers().add(memberId);
            }
        });
        taskRepository.save(task);
    }

    public void acceptTaskInvitation(String taskId, String userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required to accept a task invitation");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task.Invitation invitation = task.getInvitations().stream()
                .filter(inv -> inv.getUserId().equals(userId) && inv.getStatus() == Task.InvitationStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No pending invitation found for this user"));

        invitation.setStatus(Task.InvitationStatus.ACCEPTED);
        taskRepository.save(task);

        user.getTaskInvitations().stream()
                .filter(inv -> inv.getUserId().equals(taskId) && inv.getStatus() == Task.InvitationStatus.PENDING)
                .forEach(inv -> inv.setStatus(Task.InvitationStatus.ACCEPTED));

        user.addTask(task); // Add the task to the user's task list
        userRepository.save(user);
    }

    public void rejectTaskInvitation(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (task.getInvitedUsers().contains(userId)) {
            task.getInvitedUsers().remove(userId);
            taskRepository.save(task);
        }
    }

    public void leaveTask(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (task.getInvitedUsers().contains(userId)) {
            task.getInvitedUsers().remove(userId);
            user.getTaskInvitations().remove(taskId);
            taskRepository.save(task);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User is not part of the task");
        }
    }

    public void leaveAcceptedTask(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove the user from the task's invitations list
        task.getInvitations().removeIf(invitation -> 
            invitation.getUserId().equals(userId) && invitation.getStatus() == Task.InvitationStatus.ACCEPTED);

        // Remove the task from the user's taskInvitations list
        user.getTaskInvitations().removeIf(invitation -> 
            invitation.getUserId().equals(taskId) && invitation.getStatus() == Task.InvitationStatus.ACCEPTED);

        // Save the updated task and user
        taskRepository.save(task);
        userRepository.save(user);
    }

    public List<Task> getAcceptedTasks(String userId) {
        return taskRepository.findByInvitationsUserIdAndInvitationsStatus(userId, Task.InvitationStatus.ACCEPTED);
    }

    public List<Task> getEditableTasks(String userId) {
        return taskRepository.findByInvitedUsersContaining(userId).stream()
                .filter(task -> !task.getInvitedUsers().contains(userId)) // Ensure the user has accepted
                .collect(Collectors.toList());
    }

    public List<Task> getTaskInvitations(String userId) {
        return taskRepository.findByInvitationsUserIdAndInvitationsStatus(userId, Task.InvitationStatus.PENDING);
    }
}