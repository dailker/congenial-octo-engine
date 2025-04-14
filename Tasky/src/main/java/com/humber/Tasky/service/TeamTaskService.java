package com.humber.Tasky.service;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.TeamTask;
import com.humber.Tasky.model.User;
import com.humber.Tasky.repository.TaskRepository;
import com.humber.Tasky.repository.TeamRepository;
import com.humber.Tasky.repository.TeamTaskRepository;
import com.humber.Tasky.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TeamTaskService {

    @Autowired
    private TeamTaskRepository teamTaskRepository;

    @Autowired
    private TeamRepository teamRepository;

    public TeamTask getTaskById(String id) {
        return teamTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    public List<TeamTask> getAllTasks(Team owner) {
        return teamTaskRepository.findByOwner(owner);
    }

    public TeamTask completeTask(String id) {
        TeamTask task = teamTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        task.setCompleted(true);
        return teamTaskRepository.save(task);
    }
    public TeamTask uncompleteTask(String id) {
        TeamTask task = teamTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    
        task.setCompleted(false);
        
        return teamTaskRepository.save(task);
    }

    public TeamTask createTask(TeamTask task, Team team) {

        task.setCreatedBy(team);
        task.setOwner(team);
        TeamTask savedTask = teamTaskRepository.save(task);
        
        team.addTask(savedTask);
        teamRepository.save(team);
        
        return savedTask;
    }

    public TeamTask updateTask(String id, TeamTask taskDetails) {
        TeamTask task = teamTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setDueDate(taskDetails.getDueDate());
        task.setCompleted(taskDetails.isCompleted());
        task.setPriority(taskDetails.getPriority());
        
        return teamTaskRepository.save(task);
    }

    public void deleteTask(String id) {
        TeamTask task = teamTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        Team owner = task.getOwner();
        if (owner != null) {
            owner.getTasks().remove(task);
            teamRepository.save(owner);
        }
        
        teamTaskRepository.delete(task);
    }

    public List<TeamTask> getTasksByDateRange(Team owner, LocalDateTime start, LocalDateTime end) {
        return teamTaskRepository.findByOwnerAndDueDateBetween(owner, start, end);
    }

    public List<TeamTask> getCompletedTasks(Team owner, boolean completed) {
        return teamTaskRepository.findByOwnerAndCompleted(owner, completed);
    }

    public TeamTask shareTask(String taskId, Team owner, Team newOwner) {
        TeamTask task = teamTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getOwner().equals(owner)) {
            throw new RuntimeException("You don't have permission to share this task");
        }

        TeamTask sharedTask = new TeamTask();
        sharedTask.setTitle(task.getTitle());
        sharedTask.setDescription(task.getDescription());
        sharedTask.setDueDate(task.getDueDate());
        sharedTask.setPriority(task.getPriority());
        sharedTask.setOwner(newOwner);
        sharedTask.setCreatedBy(owner);

        TeamTask savedTask = teamTaskRepository.save(sharedTask);
        newOwner.addTask(savedTask);
        teamRepository.save(newOwner);
        
        return savedTask;
    }
}