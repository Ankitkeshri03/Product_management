package com.owndeck.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class ProjectDtos {
    public record CreateProjectRequest(
            @NotBlank @Size(min = 3, max = 120) String name,
            @Size(max = 1000) String description,
            LocalDate dueDate,
            List<Long> memberIds
    ) {}

    public record UpdateProjectRequest(
            @NotBlank @Size(min = 3, max = 120) String name,
            @Size(max = 1000) String description,
            LocalDate dueDate,
            List<Long> memberIds
    ) {}

    public record ProjectMemberResponse(Long id, String fullName, String email, String role) {}

    public record ProjectResponse(
            Long id,
            String name,
            String description,
            LocalDate dueDate,
            AuthDtos.UserSummary owner,
            List<ProjectMemberResponse> members,
            long totalTasks,
            long completedTasks
    ) {}
}
