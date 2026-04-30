package com.owndeck.taskmanager.controller;

import com.owndeck.taskmanager.dto.ApiMessage;
import com.owndeck.taskmanager.dto.ProjectDtos;
import com.owndeck.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectDtos.ProjectResponse> getProjects(Authentication authentication) {
        return projectService.getProjects(authentication);
    }

    @PostMapping
    public ProjectDtos.ProjectResponse createProject(@Valid @RequestBody ProjectDtos.CreateProjectRequest request,
                                                     Authentication authentication) {
        return projectService.createProject(request, authentication);
    }

    @PutMapping("/{projectId}")
    public ProjectDtos.ProjectResponse updateProject(@PathVariable Long projectId,
                                                     @Valid @RequestBody ProjectDtos.UpdateProjectRequest request,
                                                     Authentication authentication) {
        return projectService.updateProject(projectId, request, authentication);
    }

    @DeleteMapping("/{projectId}")
    public ApiMessage deleteProject(@PathVariable Long projectId, Authentication authentication) {
        projectService.deleteProject(projectId, authentication);
        return new ApiMessage("Project deleted successfully");
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ProjectDtos.ProjectResponse addMember(@PathVariable Long projectId,
                                                 @PathVariable Long userId,
                                                 Authentication authentication) {
        return projectService.addMember(projectId, userId, authentication);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ProjectDtos.ProjectResponse removeMember(@PathVariable Long projectId,
                                                    @PathVariable Long userId,
                                                    Authentication authentication) {
        return projectService.removeMember(projectId, userId, authentication);
    }
}
