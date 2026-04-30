package com.owndeck.taskmanager.dto;

import com.owndeck.taskmanager.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {}
