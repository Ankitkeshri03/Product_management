package com.owndeck.taskmanager.controller;

import com.owndeck.taskmanager.dto.ApiMessage;
import com.owndeck.taskmanager.dto.TaskDtos;
import com.owndeck.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskDtos.TaskResponse> getTasks(Authentication authentication) {
        return taskService.getTasks(authentication);
    }

    @PostMapping
    public TaskDtos.TaskResponse createTask(@Valid @RequestBody TaskDtos.CreateTaskRequest request,
                                            Authentication authentication) {
        return taskService.createTask(request, authentication);
    }

    @PutMapping("/{taskId}")
    public TaskDtos.TaskResponse updateTask(@PathVariable Long taskId,
                                            @Valid @RequestBody TaskDtos.UpdateTaskRequest request,
                                            Authentication authentication) {
        return taskService.updateTask(taskId, request, authentication);
    }

    @DeleteMapping("/{taskId}")
    public ApiMessage deleteTask(@PathVariable Long taskId, Authentication authentication) {
        taskService.deleteTask(taskId, authentication);
        return new ApiMessage("Task deleted successfully");
    }
}
