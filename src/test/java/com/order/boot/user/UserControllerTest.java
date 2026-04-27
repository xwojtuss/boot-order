package com.order.boot.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.boot.api.ApiPaths;
import com.order.boot.user.dto.CreateUserRequest;
import com.order.boot.user.dto.UpdateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void getUsersReturnsExistingUsers() throws Exception {
        userRepository.save(new User("alice", "encoded-password"));
        userRepository.save(new User("bob", "encoded-password"));

        mockMvc.perform(get(ApiPaths.V1 + "/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].username").value("bob"));
    }

    @Test
    void getUserByIdReturnsUser() throws Exception {
        User user = userRepository.save(new User("alice", "encoded-password"));

        mockMvc.perform(get(ApiPaths.V1 + "/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserByIdReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get(ApiPaths.V1 + "/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUserByUsernameReturnsUser() throws Exception {
        userRepository.save(new User("alice", "encoded-password"));

        mockMvc.perform(get(ApiPaths.V1 + "/users/search").param("username", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getUserByUsernameReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get(ApiPaths.V1 + "/users/search").param("username", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserCreatesUserAndReturnsLocation() throws Exception {
        CreateUserRequest request = new CreateUserRequest("alice", "password123");

        mockMvc.perform(post(ApiPaths.V1 + "/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesRegex(".*/api/v1/users/\\d+$")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUserReturnsConflictWhenUsernameExists() throws Exception {
        userRepository.save(new User("alice", "encoded-password"));
        CreateUserRequest request = new CreateUserRequest("alice", "password123");

        mockMvc.perform(post(ApiPaths.V1 + "/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void createUserReturnsValidationErrors() throws Exception {
        CreateUserRequest request = new CreateUserRequest("", "short");

        mockMvc.perform(post(ApiPaths.V1 + "/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username").value("Username is required"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password must be between 8 and 100 characters"));
    }

    @Test
    void updateUserUpdatesUser() throws Exception {
        User user = userRepository.save(new User("alice", "encoded-password"));
        UpdateUserRequest request = new UpdateUserRequest("alice-updated", "updated-pass");

        mockMvc.perform(put(ApiPaths.V1 + "/users/" + user.getId())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("alice-updated"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateUserReturnsNotFoundWhenMissing() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("alice-updated", "updated-pass");

        mockMvc.perform(put(ApiPaths.V1 + "/users/999")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateUserReturnsConflictWhenUsernameBelongsToAnotherUser() throws Exception {
        userRepository.save(new User("alice", "encoded-password"));
        User otherUser = userRepository.save(new User("bob", "encoded-password"));
        UpdateUserRequest request = new UpdateUserRequest("alice", "updated-pass");

        mockMvc.perform(put(ApiPaths.V1 + "/users/" + otherUser.getId())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void deleteUserRemovesUser() throws Exception {
        User user = userRepository.save(new User("alice", "encoded-password"));

        mockMvc.perform(delete(ApiPaths.V1 + "/users/" + user.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(delete(ApiPaths.V1 + "/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}
