package com.humber.Tasky.service;

import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.User;
import com.humber.Tasky.repository.TeamRepository;
import com.humber.Tasky.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team createTeam(Team team) {
        if (team.getName() == null || team.getName().trim().isEmpty()) {
            throw new RuntimeException("Team name cannot be empty");
        }
        // The team creator is already set in the controller
        return teamRepository.save(team);
    }

    public Optional<Team> getTeamById(String id) {
        return teamRepository.findById(id);
    }

    public void addMemberToTeam(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        @SuppressWarnings("unused")
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!team.getMemberIds().contains(userId)) {
            team.getMemberIds().add(userId);
            teamRepository.save(team);
        }
    }

    public void removeMemberFromTeam(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (team.getMemberIds().contains(userId)) {
            team.getMemberIds().remove(userId);
            teamRepository.save(team);
        }
    }

    public void inviteUserToTeam(String teamId, String email) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (team.getInvitations().contains(email)) {
            throw new RuntimeException("User has already been invited");
        }

        team.getInvitations().add(email);
        teamRepository.save(team);
    }

    public void addMemberToTeam(String teamId, String userId, String permission) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        if (!team.getMemberIds().contains(userId)) {
            team.getMemberIds().add(userId);
            team.getMemberPermissions().put(userId, permission);
            teamRepository.save(team);
        }
    }

    public void sendJoinRequest(String teamId, String userId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        if (team.getMemberIds().contains(userId)) {
            throw new RuntimeException("You are already a member of this team");
        }
        if (team.getInvitations().contains(userId)) {
            throw new RuntimeException("You have already sent a join request");
        }
        team.getInvitations().add(userId);
        teamRepository.save(team);
    }

    public void acceptJoinRequest(String teamId, String userId, String ownerId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (!team.getMemberPermissions().get(ownerId).equals("Owner")) {
            throw new RuntimeException("Only owner can accept requests");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!team.getInvitations().contains(user.getEmail())) {
            throw new RuntimeException("No pending request from this user");
        }
        
        team.getInvitations().remove(user.getEmail());
        team.getMemberIds().add(userId);
        team.getMemberPermissions().put(userId, "Member");
        teamRepository.save(team);
    }
    
    public void rejectJoinRequest(String teamId, String userId, String ownerId) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (!team.getMemberPermissions().get(ownerId).equals("Owner")) {
            throw new RuntimeException("Only owner can reject requests");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        team.getInvitations().remove(user.getEmail());
        teamRepository.save(team);
    }

    public void leaveTeam(String teamId, String userId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        if (!team.getMemberIds().contains(userId)) {
            throw new RuntimeException("You are not a member of this team");
        }
        if ("Owner".equals(team.getMemberPermissions().get(userId))) {
            throw new RuntimeException("The owner cannot leave the team. Transfer ownership first.");
        }
        team.getMemberIds().remove(userId);
        team.getMemberPermissions().remove(userId);
        teamRepository.save(team);
    }

    public void transferOwnership(String teamId, String currentOwnerId, String newOwnerId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        if (!"Owner".equals(team.getMemberPermissions().get(currentOwnerId))) {
            throw new RuntimeException("Only the owner can transfer ownership");
        }
        if (!team.getMemberIds().contains(newOwnerId)) {
            throw new RuntimeException("The new owner must be a member of the team");
        }
        team.getMemberPermissions().put(currentOwnerId, "User");
        team.getMemberPermissions().put(newOwnerId, "Owner");
        team.setOwnerId(newOwnerId);
        teamRepository.save(team);
    }

    public void deleteTeam(String teamId, String ownerId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        if (!"Owner".equals(team.getMemberPermissions().get(ownerId))) {
            throw new RuntimeException("Only the owner can delete the team");
        }
        teamRepository.delete(team);
    }

    public List<Team> getTeamsForUser(String userId) {
        return teamRepository.findAll().stream()
                .filter(team -> team.getMemberIds().contains(userId))
                .collect(Collectors.toList());
    }

    public List<Team> getTeamsWithInvitationsForUser(String userEmail) {
        return teamRepository.findAll().stream()
                .filter(team -> team.getInvitations().contains(userEmail))
                .collect(Collectors.toList());
    }

    public void acceptInvitation(String teamId, String userId, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getInvitations().contains(userEmail)) {
            throw new RuntimeException("No pending invitation for this team");
        }

        // Remove the invitation and add the user as a member
        team.getInvitations().remove(userEmail);
        team.getMemberIds().add(userId);
        team.getMemberPermissions().put(userId, "Member");
        teamRepository.save(team);
    }

    public void rejectInvitation(String teamId, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        team.getInvitations().remove(userEmail);
        teamRepository.save(team);
    }
}


