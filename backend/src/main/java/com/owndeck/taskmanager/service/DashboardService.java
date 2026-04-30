package com.owndeck.taskmanager.service;

import com.owndeck.taskmanager.dto.DashboardResponse;
import com.owndeck.taskmanager.model.TaskStatus;
import com.owndeck.taskmanager.repository.TaskRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final MapperService mapperService;

    public DashboardService(ProjectService projectService, TaskRepository taskRepository, UserService userService, MapperService mapperService) {
        this.projectService = projectService;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.mapperService = mapperService;
    }

    public DashboardResponse getDashboard(Authentication authentication) {
        var projects = projectService.getProjects(authentication);
        var currentUser = userService.getCurrentUser(authentication);
        var projectIds = projects.stream().map(project -> project.id()).toList();
        var tasks = projectIds.isEmpty() ? java.util.List.<com.owndeck.taskmanager.model.Task>of() : taskRepository.findByProjectIds(projectIds);
        long myTasks = tasks.stream().filter(task -> task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId())).count();
        long overdueTasks = tasks.stream().filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.DONE).count();
        Map<String, Long> statusBreakdown = Arrays.stream(TaskStatus.values())
                .collect(Collectors.toMap(Enum::name, status -> tasks.stream().filter(task -> task.getStatus() == status).count()));

        var recentTasks = tasks.stream()
                .sorted(Comparator.comparing(com.owndeck.taskmanager.model.Task::getCreatedAt).reversed())
                .limit(6)
                .map(mapperService::toTaskResponse)
                .toList();

        return new DashboardResponse(projects.size(), tasks.size(), myTasks, overdueTasks, statusBreakdown, recentTasks);
    }
}
