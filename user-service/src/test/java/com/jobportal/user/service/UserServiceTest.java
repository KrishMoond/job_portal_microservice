package com.jobportal.user.service;

import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ResourceNotFoundException;
import com.jobportal.user.dto.RegisterRequest;
import com.jobportal.user.dto.UserResponse;
import com.jobportal.user.dto.UserUpdateRequest;
import com.jobportal.user.model.User;
import com.jobportal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private RegisterRequest req;
    private User savedUser;

    @BeforeEach
    void setUp() {
        req = new RegisterRequest();
        req.setName("John Doe");
        req.setEmail("john@example.com");
        req.setPassword("Secret@123");
        req.setRole(User.Role.JOB_SEEKER);

        savedUser = new User();
        savedUser.setId("user-1");
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setPassword("encoded");
        savedUser.setRole(User.Role.JOB_SEEKER);
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(req);

        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Email already registered");
    }

    @Test
    void getById_found() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(savedUser));

        UserResponse response = userService.getById("user-1");

        assertThat(response.getUserId()).isEqualTo("user-1");
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(userRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById("bad-id"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_success() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

        UserResponse response = userService.update("user-1", req);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_emailTakenByOther_throwsBadRequest() {
        savedUser.setEmail("old@example.com");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(savedUser));
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.update("user-1", req))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Email already in use");
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update("missing", req))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_sameEmailAndBlankPassword_doesNotEncodePassword() {
        req.setPassword(" ");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.update("user-1", req);

        assertThat(response.getUserId()).isEqualTo("user-1");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateProfile_updatesProvidedFields() {
        UserUpdateRequest update = new UserUpdateRequest();
        update.setName("Jane");
        update.setPhone("1234567890");
        update.setLocation("Remote");
        update.setExperienceLevel(User.ExperienceLevel.SENIOR);
        update.setEducation("MS");
        update.setPreferredJobTypes("FULL_TIME");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateProfile("user-1", update);

        assertThat(response.getName()).isEqualTo("Jane");
        assertThat(response.getPhone()).isEqualTo("1234567890");
        assertThat(response.getLocation()).isEqualTo("Remote");
        assertThat(response.getExperienceLevel()).isEqualTo(User.ExperienceLevel.SENIOR);
        assertThat(response.getEducation()).isEqualTo("MS");
        assertThat(response.getPreferredJobTypes()).isEqualTo("FULL_TIME");
    }

    @Test
    void updateProfile_ignoresNullFields() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateProfile("user-1", new UserUpdateRequest());

        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getPhone()).isNull();
        assertThat(response.getLocation()).isNull();
    }

    @Test
    void updateProfile_notFound_throwsResourceNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile("missing", new UserUpdateRequest()))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
