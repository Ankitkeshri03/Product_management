package com.owndeck.taskmanager.dto;

import com.owndeck.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class TaskDtos {
    public record CreateTaskRequest(
            @NotBlank @Size(min = 3, max = 160) String title,
            @Size(max = 1000) String description,
            @NotNull Long projectId,
            Long assignedToId,
            LocalDate dueDate,
            @NotNull TaskStatus status
    ) {}

    public record UpdateTaskRequest(
            @NotBlank @Size(min = 3, max = 160) String title,
            @Size(max = 1000) String description,
            Long assignedToId,
            LocalDate dueDate,
            @NotNull TaskStatus status
    ) {}

    public record TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDate dueDate,
            Long projectId,
            String projectName,
            AuthDtos.UserSummary assignedTo,
            AuthDtos.UserSummary createdBy,
            boolean overdue
    ) {}
}
