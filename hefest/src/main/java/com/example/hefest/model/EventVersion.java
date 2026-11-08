package com.example.hefest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class EventVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    private int versionNumber;
    private String title;
    @Column(columnDefinition = "TEXT") private String description;
    private LocalDate date;
    private LocalTime time;
    private BigDecimal price;
    private int capacity;
    @Enumerated(EnumType.STRING) private EventStatus status;
    private String changedBy;
    private LocalDateTime createdAt = LocalDateTime.now();

    protected EventVersion() { }

    public EventVersion(Event event, int versionNumber, String changedBy) {
        this.eventId = event.getId(); this.versionNumber = versionNumber;
        this.title = event.getTitle(); this.description = event.getDescription();
        this.date = event.getDate(); this.time = event.getTime(); this.price = event.getPrice();
        this.capacity = event.getCapacity(); this.status = event.getStatus(); this.changedBy = changedBy;
    }
    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public int getVersionNumber() { return versionNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public BigDecimal getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public EventStatus getStatus() { return status; }
    public String getChangedBy() { return changedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
