package com.humber.Tasky.service;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.TeamEntry;
import com.humber.Tasky.model.User;
import com.humber.Tasky.repository.TeamEntryRepository;
import com.humber.Tasky.repository.TeamRepository;
import com.humber.Tasky.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamEntryRepository teamEntryRepository;
    
    @Autowired
    private UserRepository userRepository;

    public Team getTeamById(String id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
    }

    public List<Team> getAllTeams(User owner) {
        Optional<Team> team;
        List<Team> teams;
        List<TeamEntry> teamEntries;
        if (owner == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        teams = new ArrayList<>();
        teamEntries = teamEntryRepository.findByUserId(owner.getId());
        for (TeamEntry teamEntry : teamEntries) {
            team = teamRepository.findById(teamEntry.getTeamId());
            if (team.isPresent()) {
                teams.add(team.get());
            }
        }
        return teams;
    }

    public Team createTeam(Team team) {
        TeamEntry teamEntry;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User must be authenticated to create a team");
        }
        
        String email = authentication.getName();
        User createdBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        team.addMember(createdBy.getId());
        Team savedTeam = teamRepository.save(team);
        
        userRepository.save(createdBy);

        teamEntry = new TeamEntry(team.getId(),createdBy.getId());

        teamEntryRepository.save(teamEntry);

        return savedTeam;
    }

    public Team updateTeam(String id, Team teamDetails) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        team.setName(teamDetails.getName());
        team.setDescription(teamDetails.getDescription());
        team.setMembers(teamDetails.getMembers());
        
        return teamRepository.save(team);
    }

    public void deleteTeam(String id) {
        List<TeamEntry> teamEntries = teamEntryRepository.findByTeamId(id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        teamEntryRepository.deleteAll(teamEntries);
        
        teamRepository.delete(team);
    }

    public Team shareTeam(String teamId, User owner, User newOwner) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Team sharedTeam = new Team();
        sharedTeam.setName(team.getName());
        sharedTeam.setDescription(team.getDescription());

        Team savedTeam = teamRepository.save(sharedTeam);

        return savedTeam;
    }
}