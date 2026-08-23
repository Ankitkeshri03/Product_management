package com.owndeck.taskmanager.service;

import com.owndeck.taskmanager.dto.TaskDtos;
import com.owndeck.taskmanager.model.Project;
import com.owndeck.taskmanager.model.Role;
import com.owndeck.taskmanager.model.Task;
import com.owndeck.taskmanager.model.User;
import com.owndeck.taskmanager.repository.ProjectMemberRepository;
import com.owndeck.taskmanager.repository.TaskRepository;
import com.owndeck.taskmanager.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;
    private final MapperService mapperService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ProjectService projectService,
                       ProjectMemberRepository projectMemberRepository, UserService userService, MapperService mapperService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
        this.projectMemberRepository = projectMemberRepository;
        this.userService = userService;
        this.mapperService = mapperService;
    }

    @Transactional
    public TaskDtos.TaskResponse createTask(TaskDtos.CreateTaskRequest request, Authentication authentication) {
        User actor = userService.getCurrentUser(authentication);
        Project project = projectService.requireProjectAccess(request.projectId(), actor);

        Task task = new Task();
        applyTaskChanges(task, project, request.title(), request.description(), request.status(), request.dueDate(), request.assignedToId());
        task.setProject(project);
        task.setCreatedBy(actor);
        taskRepository.save(task);
        return mapperService.toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDtos.TaskResponse> getTasks(Authentication authentication) {
        var projects = projectService.getProjects(authentication);
        List<Long> projectIds = projects.stream().map(project -> project.id()).toList();
        if (projectIds.isEmpty()) {
            return List.of();
        }
        return taskRepository.findByProjectIds(projectIds).stream()
                .map(mapperService::toTaskResponse)
                .toList();
    }

    @Transactional
    public TaskDtos.TaskResponse updateTask(Long taskId, TaskDtos.UpdateTaskRequest request, Authentication authentication) {
        User actor = userService.getCurrentUser(authentication);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        Project project = projectService.requireProjectAccess(task.getProject().getId(), actor);

        applyTaskChanges(task, project, request.title(), request.description(), request.status(), request.dueDate(), request.assignedToId());
        taskRepository.save(task);
        return mapperService.toTaskResponse(task);
    }

    @Transactional
    public void deleteTask(Long taskId, Authentication authentication) {
        User actor = userService.getCurrentUser(authentication);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found"));
        projectService.requireProjectAccess(task.getProject().getId(), actor);
        taskRepository.delete(task);
    }

    private void applyTaskChanges(Task task, Project project, String title, String description, com.owndeck.taskmanager.model.TaskStatus status,
                                  java.time.LocalDate dueDate, Long assignedToId) {
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setAssignedTo(resolveAssignment(project, assignedToId));
    }

    private User resolveAssignment(Project project, Long assignedToId) {
        if (assignedToId == null) {
            return null;
        }
        User assignee = userRepository.findById(assignedToId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assigned user not found"));
        boolean allowed = project.getOwner().getId().equals(assignedToId)
                || projectMemberRepository.existsByProjectIdAndUserId(project.getId(), assignedToId)
                || assignee.getRole() == Role.ROLE_ADMIN;
        if (!allowed) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned user must be a project member or admin");
        }
        return assignee;
    }
}