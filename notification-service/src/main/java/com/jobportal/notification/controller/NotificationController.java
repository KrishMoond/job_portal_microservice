package com.jobportal.notification.controller;

import com.jobportal.notification.model.InAppNotification;
import com.jobportal.notification.repository.InAppNotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final InAppNotificationRepository repository;

    public NotificationController(InAppNotificationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<InAppNotification>> getMyNotifications(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        return ResponseEntity.ok(repository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<InAppNotification>> getUnreadNotifications(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        return ResponseEntity.ok(repository.findByUserIdAndIsReadFalse(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        return repository.findById(id).map(notif -> {
            if (!notif.getUserId().equals(userId))
                return ResponseEntity.status(403).body("Access denied");
            notif.setRead(true);
            repository.save(notif);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        List<InAppNotification> unread = repository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
        return ResponseEntity.ok().build();
    }
}
