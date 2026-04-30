package com.owndeck.taskmanager.dto;

import com.owndeck.taskmanager.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record SignupRequest(
            @NotBlank @Size(min = 3, max = 80) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 64)
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain letters and numbers")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(String token, UserSummary user) {}

    public record UserSummary(Long id, String fullName, String email, Role role) {}
}
