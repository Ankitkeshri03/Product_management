package com.owndeck.taskmanager.service;

import com.owndeck.taskmanager.dto.AuthDtos;
import com.owndeck.taskmanager.dto.UpdateRoleRequest;
import com.owndeck.taskmanager.model.User;
import com.owndeck.taskmanager.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final MapperService mapperService;

    public UserService(UserRepository userRepository, MapperService mapperService) {
        this.userRepository = userRepository;
        this.mapperService = mapperService;
    }

    public List<AuthDtos.UserSummary> getAllUsers() {
        return userRepository.findAll().stream().map(mapperService::toUserSummary).toList();
    }

    public AuthDtos.UserSummary updateRole(Long userId, UpdateRoleRequest request, Authentication authentication) {
        User actor = getCurrentUser(authentication);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (actor.getId().equals(target.getId()) && target.getRole() != request.role()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        }

        target.setRole(request.role());
        userRepository.save(target);
        return mapperService.toUserSummary(target);
    }

    public User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
