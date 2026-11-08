package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.model.EventStatus;
import com.example.hefest.model.Role;
import com.example.hefest.model.User;
import com.example.hefest.model.WorkflowHistory;
import com.example.hefest.repository.EventRepository;
import com.example.hefest.repository.WorkflowHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class WorkflowService {
    private final EventRepository eventRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final EventVersionService versionService;

    public WorkflowService(EventRepository eventRepository, WorkflowHistoryRepository historyRepository,
                           EventVersionService versionService) {
        this.eventRepository = eventRepository; this.historyRepository = historyRepository; this.versionService = versionService;
    }

    @Transactional
    public void transition(Long eventId, EventStatus target, User actor) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Събитието не съществува."));
        EventStatus from = event.getStatus() == null ? EventStatus.DRAFT : event.getStatus();
        Role role = actor.getRole();
        boolean allowed = (role == Role.EDITOR && from == EventStatus.DRAFT && target == EventStatus.REVIEW)
                || (role == Role.ADMIN && from == EventStatus.REVIEW && (target == EventStatus.APPROVED || target == EventStatus.REJECTED))
                || (role == Role.ADMIN && from == EventStatus.REJECTED && target == EventStatus.DRAFT);
        if (!allowed) throw new IllegalStateException("Невалиден преход от " + from + " към " + target + ".");
        event.setStatus(target);
        eventRepository.save(event);
        historyRepository.save(new WorkflowHistory(eventId, from, target, actor.getEmail()));
        versionService.saveVersion(event, actor);
    }

    @Transactional(readOnly = true)
    public List<WorkflowHistory> history(Long eventId) { return historyRepository.findByEventIdOrderByCreatedAtDesc(eventId); }
}
