package com.example.hefest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

@Entity
public class WorkflowHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long eventId;
    @Enumerated(EnumType.STRING) private EventStatus fromStatus;
    @Enumerated(EnumType.STRING) private EventStatus toStatus;
    private String changedBy;
    private LocalDateTime createdAt = LocalDateTime.now();

    protected WorkflowHistory() { }
    public WorkflowHistory(Long eventId, EventStatus fromStatus, EventStatus toStatus, String changedBy) {
        this.eventId = eventId; this.fromStatus = fromStatus; this.toStatus = toStatus; this.changedBy = changedBy;
    }
    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public EventStatus getFromStatus() { return fromStatus; }
    public EventStatus getToStatus() { return toStatus; }
    public String getChangedBy() { return changedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
