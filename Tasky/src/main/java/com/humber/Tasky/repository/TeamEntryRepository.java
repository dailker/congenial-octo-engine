package com.humber.Tasky.repository;

import com.humber.Tasky.model.TeamEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamEntryRepository extends MongoRepository<TeamEntry, String> {
    Optional<TeamEntry> findById(String id);
    List<TeamEntry> findByTeamId(String teamId);
    List<TeamEntry> findByUserId(String userId);
}