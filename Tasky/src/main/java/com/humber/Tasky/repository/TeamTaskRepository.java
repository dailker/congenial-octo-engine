package com.humber.Tasky.repository;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.TeamTask;
import com.humber.Tasky.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TeamTaskRepository extends MongoRepository<TeamTask, String> {
    List<TeamTask> findByOwner(Team owner);
    List<TeamTask> findByOwnerAndDueDateBetween(Team owner, LocalDateTime start, LocalDateTime end);
    List<TeamTask> findByOwnerAndCompleted(Team owner, boolean completed);
}