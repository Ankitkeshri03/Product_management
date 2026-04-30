package com.owndeck.taskmanager.config;

import com.owndeck.taskmanager.model.Role;
import com.owndeck.taskmanager.model.User;
import com.owndeck.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner bootstrapAdmin(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     @Value("${app.bootstrap.admin.enabled:false}") boolean bootstrapAdminEnabled,
                                     @Value("${app.bootstrap.admin.email}") String adminEmail,
                                     @Value("${app.bootstrap.admin.password}") String adminPassword,
                                     @Value("${app.bootstrap.admin.full-name}") String adminFullName) {
        return args -> {
            if (!bootstrapAdminEnabled) {
                return;
            }
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }
            User admin = new User();
            admin.setFullName(adminFullName);
            admin.setEmail(adminEmail.toLowerCase());
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
        };
    }
}
