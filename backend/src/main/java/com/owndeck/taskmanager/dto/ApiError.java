package com.owndeck.taskmanager.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(LocalDateTime timestamp, int status, String error, Map<String, String> validations) {}