package com.humber.Tasky.controller;

import com.humber.Tasky.model.Task;
import com.humber.Tasky.model.Team;
import com.humber.Tasky.model.User;
import com.humber.Tasky.service.TaskService;
import com.humber.Tasky.service.TeamService;
import com.humber.Tasky.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    public TeamController(TeamService teamService, UserService userService) {
        this.teamService = teamService;
        this.userService = userService;
    }

    @GetMapping
    public List<Team> getAllTeams(@AuthenticationPrincipal User user) {
        //print teams for debugging
        System.out.println(" - Teams: " + teamService.getAllTeams(user));
        return teamService.getAllTeams(user);
    }

    @PostMapping
    public Team createTeam(@RequestBody Team team) {
        return teamService.createTeam(team);
    }

    @PutMapping("/{id}")
    public Team updateTeam(@PathVariable String id, @RequestBody Team teamDetails) {
        return teamService.updateTeam(id, teamDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable String id) {
        teamService.deleteTeam(id);
    }

    @PostMapping("/{teamId}/share")
    public Team shareTeam(
            @PathVariable String teamId,
            @RequestParam String recipientEmail,
            @AuthenticationPrincipal User user) {
        return teamService.shareTeam(teamId, user,
                userService.getUserByEmail(recipientEmail));
    }
}