package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.model.User;
import com.example.hefest.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final EventVersionService versionService;

    public EventService(EventRepository eventRepository, AuditLogService auditLogService,
                        NotificationService notificationService, EventVersionService versionService) {
        this.eventRepository = eventRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.versionService = versionService;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findByDeletedAtIsNull();
    }

    @Cacheable(cacheNames = "events", key = "#id")
    public Event getEvent(Long id) {
        return eventRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow();
    }

    @CacheEvict(cacheNames = "events", allEntries = true)
    public Event createEvent(Event event, User actor) {
        Event saved = eventRepository.save(event);
        auditLogService.logEvent("CREATE", saved, null, actor);
        versionService.saveVersion(saved, actor);
        notificationService.notifyAllActive("Ново събитие", "Добавено е ново събитие: " + saved.getTitle());
        return saved;
    }

    @CacheEvict(cacheNames = "events", allEntries = true)
    public Event updateEvent(Long id, Event form, User actor) {
        Event event = getEvent(id);
        if (form.getVersion() != null && !form.getVersion().equals(event.getVersion())) {
            throw new ConcurrentEditException();
        }
        Event old = copy(event);
        event.setTitle(form.getTitle());
        event.setDescription(form.getDescription());
        event.setDate(form.getDate());
        event.setTime(form.getTime());
        event.setPrice(form.getPrice());
        event.setCapacity(form.getCapacity());
        Event saved = eventRepository.save(event);
        auditLogService.logEvent("UPDATE", saved, old, actor);
        versionService.saveVersion(saved, actor);
        notificationService.notifyAllActive("Променено събитие", "Променено е събитието: " + saved.getTitle());
        return saved;
    }

    @CacheEvict(cacheNames = "events", allEntries = true)
    public void deleteEvent(Long id, User actor) {
        Event event = getEvent(id);
        Event old = copy(event);
        event.softDelete(actor == null ? "Система" : actor.getEmail());
        eventRepository.save(event);
        auditLogService.logEvent("DELETE", old, old, actor);
        notificationService.notifyAllActive("Изтрито събитие", "Изтрито е събитието: " + old.getTitle());
    }

    public List<Event> deletedEvents() { return eventRepository.findByDeletedAtIsNotNullOrderByDeletedAtDesc(); }

    @CacheEvict(cacheNames = "events", allEntries = true)
    public void restoreEvent(Long id, User actor) {
        Event event = eventRepository.findById(id).orElseThrow();
        if (event.getDeletedAt() == null) return;
        event.restore();
        Event saved = eventRepository.save(event);
        auditLogService.logEvent("RESTORE", saved, null, actor);
    }

    public Page<Event> searchEvents(String title, LocalDate fromDate, LocalDate toDate,
                                    BigDecimal minPrice, BigDecimal maxPrice, Integer minCapacity,
                                    String sort, String direction, int page, int size) {
        String property = switch (sort == null ? "" : sort) {
            case "title", "date", "price", "capacity" -> sort;
            default -> "date";
        };
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(dir, property).and(Sort.by(Sort.Direction.ASC, "id")));
        return eventRepository.search(blankToNull(title), fromDate, toDate, minPrice, maxPrice, minCapacity, pageable);
    }

    private static String blankToNull(String value) { return value == null || value.isBlank() ? "" : value.trim(); }

    private static Event copy(Event source) {
        Event copy = new Event();
        copy.setId(source.getId()); copy.setVersion(source.getVersion()); copy.setTitle(source.getTitle()); copy.setDescription(source.getDescription());
        copy.setDate(source.getDate()); copy.setTime(source.getTime()); copy.setPrice(source.getPrice());
        copy.setCapacity(source.getCapacity()); copy.setStatus(source.getStatus());
        return copy;
    }
    public static class ConcurrentEditException extends RuntimeException {
        public ConcurrentEditException() { super("Записът е променен от друг потребител. Презаредете актуалните данни."); }
    }
    public List<Event> getSortedEvents() {
        return eventRepository.findAll().stream()
                .filter(event -> event.getDeletedAt() == null)
                .sorted(Comparator.comparing(Event::getId))
                .toList();
    }
}
