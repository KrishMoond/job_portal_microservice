package com.jobportal.notification.controller;

import com.jobportal.notification.model.InAppNotification;
import com.jobportal.notification.repository.InAppNotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean InAppNotificationRepository repository;

    @Test
    void getMyNotifications_shouldReturnOk() throws Exception {
        when(repository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/notifications").header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getUnreadNotifications_shouldReturnOk() throws Exception {
        when(repository.findByUserIdAndIsReadFalse("user-1")).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/notifications/unread").header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void markAsRead_found_shouldReturnOk() throws Exception {
        InAppNotification notif = new InAppNotification();
        notif.setId("notif-1");
        notif.setUserId("user-1");
        notif.setMessage("test");
        when(repository.findById("notif-1")).thenReturn(Optional.of(notif));
        when(repository.save(any())).thenReturn(notif);

        mockMvc.perform(put("/api/notifications/notif-1/read").header("X-User-Id", "user-1"))
                .andExpect(status().isOk());
    }

    @Test
    void markAsRead_notFound_shouldReturn404() throws Exception {
        when(repository.findById("bad-id")).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/notifications/bad-id/read").header("X-User-Id", "user-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void markAsRead_wrongUser_shouldReturn403() throws Exception {
        InAppNotification notif = new InAppNotification();
        notif.setId("notif-1");
        notif.setUserId("other-user");
        notif.setMessage("test");
        when(repository.findById("notif-1")).thenReturn(Optional.of(notif));

        mockMvc.perform(put("/api/notifications/notif-1/read").header("X-User-Id", "user-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void markAllAsRead_shouldReturnOk() throws Exception {
        when(repository.findByUserIdAndIsReadFalse("user-1")).thenReturn(Collections.emptyList());
        mockMvc.perform(put("/api/notifications/read-all").header("X-User-Id", "user-1"))
                .andExpect(status().isOk());
    }
}
