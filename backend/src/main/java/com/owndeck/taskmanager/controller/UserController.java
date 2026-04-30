package com.owndeck.taskmanager.controller;

import com.owndeck.taskmanager.dto.AuthDtos;
import com.owndeck.taskmanager.dto.UpdateRoleRequest;
import com.owndeck.taskmanager.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AuthDtos.UserSummary> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/me")
    public AuthDtos.UserSummary getCurrentUser(Authentication authentication) {
        var currentUser = userService.getCurrentUser(authentication);
        return new AuthDtos.UserSummary(
                currentUser.getId(),
                currentUser.getFullName(),
                currentUser.getEmail(),
                currentUser.getRole()
        );
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public AuthDtos.UserSummary updateRole(@PathVariable Long userId,
                                           @Valid @RequestBody UpdateRoleRequest request,
                                           Authentication authentication) {
        return userService.updateRole(userId, request, authentication);
    }
}
