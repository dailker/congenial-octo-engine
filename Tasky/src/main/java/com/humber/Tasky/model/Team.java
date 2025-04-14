package com.humber.Tasky.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "teams")
public class Team {
    @Id
    private String id;
    private String name;
    private String description;
    private List<String> members = new ArrayList<>();

    @DBRef(lazy = true)
    @JsonManagedReference("owner-tasks")
    private Set<TeamTask> tasks = new HashSet<>();

    public Team() {}

    public Team(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }
    public Set<TeamTask> getTasks() { return tasks; }

    // Business methods
    public void addMember(String memberId) {
        if (!members.contains(memberId)) {
            members.add(memberId);
        }
    }
    public void addTask(TeamTask task) {
        tasks.add(task);
    }

    public void removeMember(String memberId) {
        members.remove(memberId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team user = (Team) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}