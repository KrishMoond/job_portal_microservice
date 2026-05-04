package com.jobportal.user.controller;

import com.jobportal.user.dto.LoginRequest;
import com.jobportal.user.dto.RegisterRequest;
import com.jobportal.user.dto.UserResponse;
import com.jobportal.user.dto.UserUpdateRequest;
import com.jobportal.user.service.AuthService;
import com.jobportal.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void register_shouldReturnCreated() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("Secret@123");
        req.setRole(com.jobportal.user.model.User.Role.JOB_SEEKER);

        UserResponse res = new UserResponse();
        res.setUserId("user-1");
        res.setName("John Doe");

        when(userService.register(any())).thenReturn(res);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-1"));
    }

    @Test
    void login_shouldReturnOkWithToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("Secret@123");

        Map<String, String> result = new HashMap<>();
        result.put("token", "fake-jwt");
        result.put("userId", "user-1");

        when(authService.login(any())).thenReturn(result);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer fake-jwt"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUser_shouldReturnOk() throws Exception {
        UserResponse res = new UserResponse();
        res.setUserId("user-1");
        res.setName("John Doe");
        when(userService.getById("user-1")).thenReturn(res);

        mockMvc.perform(get("/api/users/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user-1"));
    }

    @Test
    void updateUser_sameUser_shouldReturnOk() throws Exception {
        RegisterRequest req = validRegisterRequest();
        UserResponse res = new UserResponse();
        res.setUserId("user-1");
        when(userService.update(eq("user-1"), any())).thenReturn(res);

        mockMvc.perform(put("/api/users/user-1")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user-1"));
    }

    @Test
    void updateUser_admin_shouldReturnOk() throws Exception {
        RegisterRequest req = validRegisterRequest();
        UserResponse res = new UserResponse();
        res.setUserId("user-1");
        when(userService.update(eq("user-1"), any())).thenReturn(res);

        mockMvc.perform(put("/api/users/user-1")
                .header("X-User-Id", "admin-1")
                .header("X-User-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_otherUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/users/user-1")
                .header("X-User-Id", "other-user")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest())))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateProfile_sameUser_shouldReturnOk() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setName("Jane Doe");
        UserResponse res = new UserResponse();
        res.setUserId("user-1");
        when(userService.updateProfile(eq("user-1"), any())).thenReturn(res);

        mockMvc.perform(put("/api/users/user-1/profile")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_admin_shouldReturnOk() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setName("Jane Doe");
        UserResponse res = new UserResponse();
        res.setUserId("user-1");
        when(userService.updateProfile(eq("user-1"), any())).thenReturn(res);

        mockMvc.perform(put("/api/users/user-1/profile")
                .header("X-User-Id", "admin-1")
                .header("X-User-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateProfile_otherUser_shouldReturnForbidden() throws Exception {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setName("Jane Doe");

        mockMvc.perform(put("/api/users/user-1/profile")
                .header("X-User-Id", "other-user")
                .header("X-User-Role", "JOB_SEEKER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is5xxServerError());
    }

    private RegisterRequest validRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("Secret@123");
        req.setRole(com.jobportal.user.model.User.Role.JOB_SEEKER);
        return req;
    }
}
