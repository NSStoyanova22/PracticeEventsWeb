package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.model.EventStatus;
import com.example.hefest.repository.EventRepository;
import com.example.hefest.repository.RegistrationRepository;
import com.example.hefest.repository.NotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EventReminderScheduler {
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;

    public EventReminderScheduler(EventRepository eventRepository, RegistrationRepository registrationRepository,
                                  NotificationRepository notificationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processEvents() {
        LocalDate today = LocalDate.now();
        for (Event event : eventRepository.findByDeletedAtIsNullAndDateAndStatus(today.plusDays(1), EventStatus.APPROVED)) {
            for (var registration : registrationRepository.findByEventId(event.getId())) {
                String key = "EVENT_REMINDER_" + event.getId() + "_" + today;
                if (!notificationRepository.existsByUserIdAndReferenceKey(registration.getUser().getId(), key)) {
                    notificationRepository.save(new com.example.hefest.model.Notification(registration.getUser(),
                            "Напомняне за събитие", "Утре е събитието: " + event.getTitle(), key));
                }
            }
        }
        for (Event event : eventRepository.findByDeletedAtIsNullAndDateBeforeAndStatus(today, EventStatus.APPROVED)) {
            event.setStatus(EventStatus.OVERDUE);
            for (var registration : registrationRepository.findByEventId(event.getId())) {
                String key = "EVENT_OVERDUE_" + event.getId();
                if (!notificationRepository.existsByUserIdAndReferenceKey(registration.getUser().getId(), key)) {
                    notificationRepository.save(new com.example.hefest.model.Notification(registration.getUser(),
                            "Просрочено събитие", "Събитието приключи: " + event.getTitle(), key));
                }
            }
        }
    }
}
