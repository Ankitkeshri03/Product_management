package com.owndeck.taskmanager.service;

import com.owndeck.taskmanager.dto.AuthDtos;
import com.owndeck.taskmanager.dto.ProjectDtos;
import com.owndeck.taskmanager.dto.TaskDtos;
import com.owndeck.taskmanager.model.Project;
import com.owndeck.taskmanager.model.ProjectMember;
import com.owndeck.taskmanager.model.Task;
import com.owndeck.taskmanager.model.TaskStatus;
import com.owndeck.taskmanager.model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MapperService {
    public AuthDtos.UserSummary toUserSummary(User user) {
        return new AuthDtos.UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    public ProjectDtos.ProjectMemberResponse toProjectMember(ProjectMember member) {
        return new ProjectDtos.ProjectMemberResponse(
                member.getUser().getId(),
                member.getUser().getFullName(),
                member.getUser().getEmail(),
                member.getUser().getRole().name()
        );
    }

    public ProjectDtos.ProjectResponse toProjectResponse(Project project, long totalTasks, long completedTasks) {
        List<ProjectDtos.ProjectMemberResponse> members = project.getMembers().stream()
                .map(this::toProjectMember)
                .toList();
        return new ProjectDtos.ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getDueDate(),
                toUserSummary(project.getOwner()),
                members,
                totalTasks,
                completedTasks
        );
    }

    public TaskDtos.TaskResponse toTaskResponse(Task task) {
        boolean overdue = task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.DONE;
        return new TaskDtos.TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignedTo() == null ? null : toUserSummary(task.getAssignedTo()),
                toUserSummary(task.getCreatedBy()),
                overdue
        );
    }
}
