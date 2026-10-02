package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.model.Registration;
import com.example.hefest.model.User;
import com.example.hefest.repository.EventRepository;
import com.example.hefest.repository.RegistrationRepository;
import com.example.hefest.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    public RegistrationService(EventRepository eventRepository,
                               UserRepository userRepository,
                               RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public void register(Long eventId, User authenticatedUser) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUser.getId()));

        if (registrationRepository.existsByEventIdAndUserId(event.getId(), user.getId())) {
            throw new DuplicateRegistrationException();
        }

        if (registrationRepository.countByEventId(event.getId()) >= event.getCapacity()) {
            throw new EventFullException();
        }

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setUser(user);
        registrationRepository.save(registration);
    }

    @Transactional
    public void cancel(Long eventId, User authenticatedUser) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUser.getId()));

        Registration registration = registrationRepository.findByEventIdAndUserId(event.getId(), user.getId())
                .orElseThrow(() -> new IllegalStateException("User is not registered for this event"));
        registrationRepository.delete(registration);
    }

    public long countForEvent(Long eventId) {
        return registrationRepository.countByEventId(eventId);
    }

    public boolean isRegistered(Long eventId, User authenticatedUser) {
        return authenticatedUser != null
                && registrationRepository.existsByEventIdAndUserId(eventId, authenticatedUser.getId());
    }

    public static class DuplicateRegistrationException extends RuntimeException {
    }

    public static class EventFullException extends RuntimeException {
    }
}
