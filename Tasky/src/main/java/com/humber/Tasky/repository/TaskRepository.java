package com.humber.Tasky.repository;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByOwner(User owner);
    List<Task> findByOwnerAndDueDateBetween(User owner, LocalDateTime start, LocalDateTime end);
    List<Task> findByOwnerAndCompleted(User owner, boolean completed);
}