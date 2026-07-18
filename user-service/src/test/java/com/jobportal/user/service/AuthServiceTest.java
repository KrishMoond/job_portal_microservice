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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock UserService userService;
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
        user.setEmailVerified(true);

        req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("Secret@123");
    }

    @Test
    void login_success_returnsEmail() {
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-otp");
        doNothing().when(userService).sendLoginOtpEmail(anyString(), anyString());

        String result = authService.login(req);

        assertThat(result).isEqualTo("john@example.com");
        verify(userRepository).save(user);
        verify(userService).sendLoginOtpEmail(eq("john@example.com"), anyString());
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

    @Test
    void login_unverifiedEmail_throwsBadRequest() {
        user.setEmailVerified(false);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Please verify your email before logging in");
    }

    @Test
    void verifyLoginOtp_success_returnsToken() {
        user.setVerificationOtp("hashed-otp");
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpAttempts(0);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed-otp")).thenReturn(true);
        when(jwtUtil.generateToken("user-1", "RECRUITER")).thenReturn("jwt-token");

        var result = authService.verifyLoginOtp("john@example.com", "123456");

        assertThat(result.get("userId")).isEqualTo("user-1");
        assertThat(result.get("role")).isEqualTo("RECRUITER");
    }

    @Test
    void verifyLoginOtp_wrongOtp_throwsBadRequest() {
        user.setVerificationOtp("hashed-otp");
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpAttempts(0);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("000000", "hashed-otp")).thenReturn(false);

        assertThatThrownBy(() -> authService.verifyLoginOtp("john@example.com", "000000"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Invalid OTP");
    }

    @Test
    void verifyLoginOtp_tooManyAttempts_throwsBadRequest() {
        user.setVerificationOtp("hashed-otp");
        user.setVerificationOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setOtpAttempts(3);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyLoginOtp("john@example.com", "123456"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Too many failed attempts. Please login again.");
    }

    @Test
    void verifyLoginOtp_expired_throwsBadRequest() {
        user.setVerificationOtp("hashed-otp");
        user.setVerificationOtpExpiry(LocalDateTime.now().minusMinutes(1));
        user.setOtpAttempts(0);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyLoginOtp("john@example.com", "123456"))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("OTP has expired. Please login again.");
    }
}
