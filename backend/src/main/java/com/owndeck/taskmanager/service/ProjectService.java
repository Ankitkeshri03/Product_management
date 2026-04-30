package com.owndeck.taskmanager.service;

import com.owndeck.taskmanager.dto.ProjectDtos;
import com.owndeck.taskmanager.model.Project;
import com.owndeck.taskmanager.model.ProjectMember;
import com.owndeck.taskmanager.model.TaskStatus;
import com.owndeck.taskmanager.model.User;
import com.owndeck.taskmanager.repository.ProjectMemberRepository;
import com.owndeck.taskmanager.repository.ProjectRepository;
import com.owndeck.taskmanager.repository.TaskRepository;
import com.owndeck.taskmanager.repository.UserRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final MapperService mapperService;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, UserRepository userRepository,
                          UserService userService, TaskRepository taskRepository, MapperService mapperService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.taskRepository = taskRepository;
        this.mapperService = mapperService;
    }

    @Transactional
    public ProjectDtos.ProjectResponse createProject(ProjectDtos.CreateProjectRequest request, Authentication authentication) {
        User owner = userService.getCurrentUser(authentication);
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setDueDate(request.dueDate());
        project.setOwner(owner);
        projectRepository.save(project);

        syncProjectMembers(project, request.memberIds());
        return buildProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDtos.ProjectResponse> getProjects(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        return projectRepository.findAccessibleProjects(user.getId()).stream()
                .map(this::buildProjectResponse)
                .toList();
    }

    @Transactional
    public ProjectDtos.ProjectResponse updateProject(Long projectId, ProjectDtos.UpdateProjectRequest request, Authentication authentication) {
        User actor = userService.getCurrentUser(authentication);
        Project project = requireProjectAccess(projectId, actor);
        requireProjectManagementAccess(project, actor);

        project.setName(request.name());
        project.setDescription(request.description());
        project.setDueDate(request.dueDate());
        syncProjectMembers(project, request.memberIds());

        return buildProjectResponse(project);
    }

    @Transactional
    public void deleteProject(Long projectId, Authentication authentication) {
        User actor = userService.getCurrentUser(authentication);
        Project project = requireProjectAccess(projectId, actor);
        requireProjectManagementAccess(project, actor);
        taskRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDtos.ProjectResponse addMember(Long projectId, Long userId, Authentication authentication) {
        User actor = userService.getCurrentUser(authentication);
        Project project = requireProjectAccess(projectId, actor);
        requireProjectManagementAccess(project, actor);

        if (project.getOwner().getId().equals(userId)) {
            return buildProjectResponse(project);
        }
        boolean exists = project.getMembers().stream().anyMatch(member -> member.getUser().getId().equals(userId));
        if (exists) {
            throw new ApiException(HttpStatus.CONFLICT, "User already belongs to project");
        }

        User memberUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(memberUser);
        project.getMembers().add(member);
        projectRepository.save(project);
        return buildProjectResponse(project);
    }

    @Transactional
    public ProjectDtos.ProjectResponse removeMember(Long projectId, Long userId, Authentication authentication) {
        User actor = userService.getCurrentUser(authentication);
        Project project = requireProjectAccess(projectId, actor);
        requireProjectManagementAccess(project, actor);

        if (project.getOwner().getId().equals(userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Project owner cannot be removed from the project");
        }

        boolean exists = project.getMembers().stream().anyMatch(member -> member.getUser().getId().equals(userId));
        if (!exists) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Project member not found");
        }

        taskRepository.findByProjectId(projectId).stream()
                .filter(task -> task.getAssignedTo() != null && task.getAssignedTo().getId().equals(userId))
                .forEach(task -> task.setAssignedTo(null));

        project.getMembers().removeIf(member -> member.getUser().getId().equals(userId));
        projectRepository.save(project);
        return buildProjectResponse(project);
    }

    public Project requireProjectAccess(Long projectId, User user) {
        Project project = projectRepository.findWithDetailsById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
        boolean hasAccess = project.getOwner().getId().equals(user.getId())
                || user.getRole().name().equals("ROLE_ADMIN")
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());
        if (!hasAccess) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }
        project.setMembers(projectMemberRepository.findByProjectId(projectId));
        return project;
    }

    public void requireProjectManagementAccess(Project project, User actor) {
        boolean allowed = project.getOwner().getId().equals(actor.getId()) || actor.getRole().name().equals("ROLE_ADMIN");
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins or project owners can manage this project");
        }
    }

    private void syncProjectMembers(Project project, List<Long> requestedMemberIds) {
        Set<Long> targetIds = requestedMemberIds == null ? new HashSet<>() : new HashSet<>(requestedMemberIds);
        targetIds.remove(project.getOwner().getId());

        Map<Long, ProjectMember> existingMembersByUserId = new HashMap<>();
        for (ProjectMember member : project.getMembers()) {
            existingMembersByUserId.put(member.getUser().getId(), member);
        }

        Set<Long> removableIds = new HashSet<>(existingMembersByUserId.keySet());
        removableIds.removeAll(targetIds);
        if (!removableIds.isEmpty()) {
            taskRepository.findByProjectId(project.getId()).stream()
                    .filter(task -> task.getAssignedTo() != null && removableIds.contains(task.getAssignedTo().getId()))
                    .forEach(task -> task.setAssignedTo(null));

            project.getMembers().removeIf(member -> removableIds.contains(member.getUser().getId()));
        }

        for (Long memberId : targetIds) {
            if (!existingMembersByUserId.containsKey(memberId)) {
                User memberUser = userRepository.findById(memberId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found: " + memberId));
                ProjectMember member = new ProjectMember();
                member.setProject(project);
                member.setUser(memberUser);
                project.getMembers().add(member);
            }
        }

        projectRepository.save(project);
    }

    private ProjectDtos.ProjectResponse buildProjectResponse(Project project) {
        var tasks = taskRepository.findByProjectId(project.getId());
        long completed = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        return mapperService.toProjectResponse(project, tasks.size(), completed);
    }
}
