package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.model.EventVersion;
import com.example.hefest.model.User;
import com.example.hefest.repository.EventVersionRepository;
import com.example.hefest.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EventVersionService {
    private final EventVersionRepository repository;
    private final EventRepository eventRepository;
    private final AuditLogService auditLogService;

    public EventVersionService(EventVersionRepository repository, EventRepository eventRepository,
                               AuditLogService auditLogService) {
        this.repository = repository; this.eventRepository = eventRepository; this.auditLogService = auditLogService;
    }

    @Transactional
    public EventVersion saveVersion(Event event, User actor) {
        String by = actor == null ? "Система" : actor.getEmail();
        return repository.save(new EventVersion(event, repository.countByEventId(event.getId()) + 1, by));
    }

    @Transactional(readOnly = true)
    public List<EventVersion> versions(Long eventId) { return repository.findByEventIdOrderByVersionNumberDesc(eventId); }

    @Transactional(readOnly = true)
    public EventVersion get(Long eventId, Long versionId) {
        return repository.findByIdAndEventId(versionId, eventId)
                .orElseThrow(() -> new IllegalArgumentException("Версията не съществува."));
    }

    @Transactional
    public void restore(Long eventId, Long versionId, User actor) {
        EventVersion version = get(eventId, versionId);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Събитието не съществува."));
        Event old = copy(event);
        event.setTitle(version.getTitle()); event.setDescription(version.getDescription()); event.setDate(version.getDate());
        event.setTime(version.getTime()); event.setPrice(version.getPrice()); event.setCapacity(version.getCapacity());
        event.setStatus(version.getStatus());
        Event saved = eventRepository.save(event);
        auditLogService.logEvent("RESTORE_VERSION", saved, old, actor);
        saveVersion(saved, actor);
    }

    private static Event copy(Event source) {
        Event copy = new Event(); copy.setId(source.getId()); copy.setTitle(source.getTitle()); copy.setDescription(source.getDescription());
        copy.setDate(source.getDate()); copy.setTime(source.getTime()); copy.setPrice(source.getPrice()); copy.setCapacity(source.getCapacity()); copy.setStatus(source.getStatus());
        return copy;
    }
}
