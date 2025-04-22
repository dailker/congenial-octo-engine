package com.humber.Tasky.service;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.User;
import com.humber.Tasky.model.Task.Priority;
import com.humber.Tasky.repository.TaskRepository;
import com.humber.Tasky.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;

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

    public void deleteTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
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
}