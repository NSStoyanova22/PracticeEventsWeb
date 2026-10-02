package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.repository.EventRepository;
import com.example.hefest.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public ReportService(EventRepository eventRepository,
                         RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    public List<EventReport> generateReport(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both report dates are required.");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The start date cannot be after the end date.");
        }

        return eventRepository.findByDateBetween(from, to).stream()
                .map(this::createEventReport)
                .toList();
    }

    public long getTotalRegistrations() {
        return registrationRepository.count();
    }

    private EventReport createEventReport(Event event) {
        long registrations = registrationRepository.countByEventId(event.getId());

        return new EventReport(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                registrations,
                event.getCapacity(),
                Math.max(0, event.getCapacity() - registrations)
        );
    }

    public record EventReport(
            Long eventId,
            String title,
            String description,
            LocalDate eventDate,
            long registrations,
            int capacity,
            long availableSpots
    ) {}
}
