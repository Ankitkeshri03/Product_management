package com.owndeck.taskmanager.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalProjects,
        long totalTasks,
        long myTasks,
        long overdueTasks,
        Map<String, Long> statusBreakdown,
        List<TaskDtos.TaskResponse> recentTasks
) {}
