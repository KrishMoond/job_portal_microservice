package com.jobportal.user.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.user.dto.LoginRequest;
import com.jobportal.user.dto.RegisterRequest;
import com.jobportal.user.dto.UserResponse;
import com.jobportal.user.dto.UserUpdateRequest;
import com.jobportal.user.service.AuthService;
import com.jobportal.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(userService.register(req), "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest req) {
        Map<String, String> result = authService.login(req);
        String token = result.remove("token");
        return ResponseEntity.ok()
            .header("Authorization", "Bearer " + token)
            .header("Access-Control-Expose-Headers", "Authorization")
            .body(ApiResponse.success(result, "Login successful"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(userId), "User fetched"));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId, 
            @Valid @RequestBody RegisterRequest req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String requestUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {
        
        if (!"ADMIN".equals(role) && !userId.equals(requestUserId)) {
            throw new ForbiddenException("You can only update your own profile");
        }
        
        return ResponseEntity.ok(ApiResponse.success(userService.update(userId, req), "User updated"));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String requestUserId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "JOB_SEEKER") String role) {

        if (!"ADMIN".equals(role) && !userId.equals(requestUserId)) {
            throw new ForbiddenException("You can only update your own profile");
        }

        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, req), "Profile updated"));
    }
}
