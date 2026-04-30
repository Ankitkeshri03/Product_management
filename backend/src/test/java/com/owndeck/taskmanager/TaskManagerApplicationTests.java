package com.owndeck.taskmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owndeck.taskmanager.model.Project;
import com.owndeck.taskmanager.model.ProjectMember;
import com.owndeck.taskmanager.model.Role;
import com.owndeck.taskmanager.model.Task;
import com.owndeck.taskmanager.model.TaskStatus;
import com.owndeck.taskmanager.model.User;
import com.owndeck.taskmanager.repository.ProjectMemberRepository;
import com.owndeck.taskmanager.repository.ProjectRepository;
import com.owndeck.taskmanager.repository.TaskRepository;
import com.owndeck.taskmanager.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskManagerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User owner;
    private User member;
    private User outsider;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        admin = createUser("Admin User", "admin@test.com", Role.ROLE_ADMIN);
        owner = createUser("Owner User", "owner@test.com", Role.ROLE_MEMBER);
        member = createUser("Member User", "member@test.com", Role.ROLE_MEMBER);
        outsider = createUser("Outsider User", "outsider@test.com", Role.ROLE_MEMBER);
    }

    @Test
    void adminCanUpdateProjectAndReplaceMembers() throws Exception {
        Project project = createProject("Launch Plan", owner, LocalDate.now().plusDays(7));
        addMember(project, member);

        String adminToken = login(admin.getEmail(), "Password@123");

        mockMvc.perform(put("/api/projects/{projectId}", project.getId())
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Launch Plan Updated",
                                "description", "Updated scope",
                                "dueDate", LocalDate.now().plusDays(14).toString(),
                                "memberIds", List.of(outsider.getId())
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Launch Plan Updated"))
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].email").value("outsider@test.com"));

        assertThat(projectMemberRepository.findByProjectIdAndUserId(project.getId(), member.getId())).isEmpty();
        assertThat(projectMemberRepository.findByProjectIdAndUserId(project.getId(), outsider.getId())).isPresent();
    }

    @Test
    void deletingProjectAlsoDeletesItsTasks() throws Exception {
        Project project = createProject("Cleanup", owner, LocalDate.now().plusDays(3));
        Task task = createTask(project, owner, owner, "Remove blockers");

        String ownerToken = login(owner.getEmail(), "Password@123");

        mockMvc.perform(delete("/api/projects/{projectId}", project.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project deleted successfully"));

        assertThat(projectRepository.findById(project.getId())).isEmpty();
        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void memberCannotDeleteProjectTheyDoNotManage() throws Exception {
        Project project = createProject("Restricted", owner, LocalDate.now().plusDays(10));
        addMember(project, member);

        String memberToken = login(member.getEmail(), "Password@123");

        mockMvc.perform(delete("/api/projects/{projectId}", project.getId())
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only admins or project owners can manage this project"));
    }

    @Test
    void taskDeleteEndpointRemovesTask() throws Exception {
        Project project = createProject("Task Ops", owner, LocalDate.now().plusDays(6));
        addMember(project, member);
        Task task = createTask(project, member, owner, "Verify deletion");

        String ownerToken = login(owner.getEmail(), "Password@123");

        mockMvc.perform(delete("/api/tasks/{taskId}", task.getId())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    private User createUser(String fullName, String email, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project createProject(String name, User ownerUser, LocalDate dueDate) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(name + " description");
        project.setDueDate(dueDate);
        project.setOwner(ownerUser);
        return projectRepository.save(project);
    }

    private void addMember(Project project, User user) {
        ProjectMember memberLink = new ProjectMember();
        memberLink.setProject(project);
        memberLink.setUser(user);
        projectMemberRepository.save(memberLink);
    }

    private Task createTask(Project project, User assignee, User createdBy, String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(title + " details");
        task.setProject(project);
        task.setAssignedTo(assignee);
        task.setCreatedBy(createdBy);
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(LocalDate.now().plusDays(1));
        return taskRepository.save(task);
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return node.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
