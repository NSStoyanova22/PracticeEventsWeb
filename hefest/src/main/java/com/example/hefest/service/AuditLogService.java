package com.example.hefest.service;

import com.example.hefest.model.AuditLog;
import com.example.hefest.model.Event;
import com.example.hefest.model.User;
import com.example.hefest.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void logEvent(String action, Event event, Event oldEvent, User actor) {
        repository.save(new AuditLog(actor, action, "Event", event == null ? null : event.getId(),
                snapshot(oldEvent), "DELETE".equals(action) ? null : snapshot(event)));
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> search(String actor, String entityType, LocalDate from, LocalDate to,
                                 int page, int size) {
        LocalDateTime fromDate = from == null ? null : from.atStartOfDay();
        LocalDateTime toDate = to == null ? null : to.plusDays(1).atStartOfDay();
        return repository.search(blankToNull(actor), blankToNull(entityType), fromDate, toDate,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
    }

    private static String snapshot(Event event) {
        if (event == null) return null;
        return "title=" + event.getTitle()
                + "; description=" + event.getDescription()
                + "; date=" + event.getDate()
                + "; time=" + event.getTime()
                + "; price=" + event.getPrice()
                + "; capacity=" + event.getCapacity()
                + "; status=" + event.getStatus();
    }


    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
