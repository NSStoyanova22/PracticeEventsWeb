package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.repository.EventRepository;
import com.example.hefest.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
        return generateReport(from, to, null, "date", "asc");
    }

    public List<EventReport> generateReport(LocalDate from, LocalDate to, String week,
                                            String sort, String direction) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both report dates are required.");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The start date cannot be after the end date.");
        }

        LocalDate weekFrom = null;
        LocalDate weekTo = null;
        if (week != null && !week.isBlank()) {
            try {
                String[] parts = week.split("-W");
                if (parts.length != 2) throw new IllegalArgumentException();
                java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
                weekFrom = LocalDate.of(Integer.parseInt(parts[0]), 1, 4)
                        .with(weekFields.weekOfWeekBasedYear(), Integer.parseInt(parts[1]))
                        .with(weekFields.dayOfWeek(), 1);
                weekTo = weekFrom.plusDays(6);
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("Невалидна седмица.");
            }
        }
        LocalDate filterFrom = weekFrom == null ? from : from.isAfter(weekFrom) ? from : weekFrom;
        LocalDate filterTo = weekTo == null ? to : to.isBefore(weekTo) ? to : weekTo;

        Comparator<Event> comparator = eventComparator(sort, direction);
        return eventRepository.findByDateBetween(filterFrom, filterTo).stream()
                .sorted(comparator)
                .map(this::createEventReport)
                .toList();
    }

    private Comparator<Event> eventComparator(String sort, String direction) {
        Comparator<Event> comparator = switch (sort == null ? "date" : sort.toLowerCase(Locale.ROOT)) {
            case "day" -> Comparator.comparing(event -> event.getDate().getDayOfWeek());
            case "time" -> Comparator.comparing(event -> event.getTime() == null ? LocalTime.MIN : event.getTime());
            default -> Comparator.comparing(Event::getDate);
        };
        comparator = comparator.thenComparing(Event::getDate)
                .thenComparing(event -> event.getTime() == null ? LocalTime.MIN : event.getTime())
                .thenComparing(Event::getId);
        return "desc".equalsIgnoreCase(direction) ? comparator.reversed() : comparator;
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
                event.getTime(),
                event.getPrice() == null ? BigDecimal.ZERO : event.getPrice(),
                registrations,
                event.getCapacity(),
                Math.max(0, event.getCapacity() - registrations),
                (event.getPrice() == null ? BigDecimal.ZERO : event.getPrice())
                        .multiply(BigDecimal.valueOf(registrations))
        );
    }

    public record EventReport(
            Long eventId,
            String title,
            String description,
            LocalDate eventDate,
            LocalTime eventTime,
            BigDecimal price,
            long registrations,
            int capacity,
            long availableSpots,
            BigDecimal revenue
    ) {}
}
