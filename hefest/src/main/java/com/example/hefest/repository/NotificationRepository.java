package com.example.hefest.repository;

import com.example.hefest.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
    boolean existsByUserIdAndReferenceKey(Long userId, String referenceKey);
}
