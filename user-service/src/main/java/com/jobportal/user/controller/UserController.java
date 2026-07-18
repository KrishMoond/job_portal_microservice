package com.jobportal.user.controller;

import com.jobportal.common.dto.ApiResponse;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.user.dto.LoginRequest;
import com.jobportal.user.dto.RegisterRequest;
import com.jobportal.user.dto.UserResponse;
import com.jobportal.user.dto.UserUpdateRequest;
import com.jobportal.user.dto.VerifyOtpRequest;
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
            .body(ApiResponse.success(userService.register(req), "Registration successful. Please check your email to verify your account."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyOtpRequest req) {
        userService.verifyEmail(req.getEmail(), req.getOtp());
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully. You can now log in."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            throw new com.jobportal.common.exception.BadRequestException("Email is required");
        userService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success(null, "A new OTP has been sent to your email."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest req) {
        String email = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(Map.of("email", email), "OTP sent to your email."));
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest req) {
        Map<String, String> result = authService.verifyLoginOtp(req.getEmail(), req.getOtp());
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
