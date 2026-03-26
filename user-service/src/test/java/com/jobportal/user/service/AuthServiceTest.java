package com.jobportal.user.service;

import com.jobportal.common.exception.BadRequestException;
import com.jobportal.user.dto.LoginRequest;
import com.jobportal.user.model.User;
import com.jobportal.user.repository.UserRepository;
import com.jobportal.user.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    private User user;
    private LoginRequest req;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
        user.setRole(User.Role.RECRUITER);

        req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("Secret@123");
    }

    @Test
    void login_success() {
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("user-1", "RECRUITER")).thenReturn("jwt-token");

        Map<String, String> result = authService.login(req);

        assertThat(result.get("token")).isEqualTo("jwt-token");
        assertThat(result.get("userId")).isEqualTo("user-1");
        assertThat(result.get("role")).isEqualTo("RECRUITER");
    }

    @Test
    void login_userNotFound_throwsBadRequest() {
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Invalid credentials");
    }

    @Test
    void login_wrongPassword_throwsBadRequest() {
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Invalid credentials");
    }
}
