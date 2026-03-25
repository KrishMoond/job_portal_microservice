package com.jobportal.notification.repository;

import com.jobportal.notification.model.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, String> {
    List<InAppNotification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<InAppNotification> findByUserIdAndIsReadFalse(String userId);
}