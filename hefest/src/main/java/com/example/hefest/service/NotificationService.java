package com.example.hefest.service;

import com.example.hefest.model.Notification;
import com.example.hefest.model.User;
import com.example.hefest.repository.NotificationRepository;
import com.example.hefest.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyAllActive(String title, String message) {
        userRepository.findAll().stream()
                .filter(User::isActive)
                .map(user -> new Notification(user, title, message))
                .forEach(notificationRepository::save);
    }

    @Transactional(readOnly = true)
    public List<Notification> forUser(User user) {
        return notificationRepository.findTop30ByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markRead(Long id, User user) {
        notificationRepository.findById(id)
                .filter(notification -> notification.getUser().getId().equals(user.getId()))
                .ifPresent(notification -> {
                    notification.markRead();
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markAllRead(User user) {
        forUser(user).forEach(notification -> {
            if (!notification.isRead()) notification.markRead();
        });
    }
}
