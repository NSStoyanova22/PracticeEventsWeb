package com.example.hefest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotBlank(message = "Заглавието е задължително.")
    private String title;

    @NotBlank(message = "Описанието е задължително.")
    private String description;

    @NotNull(message = "Датата е задължителна.")
    private LocalDate date;

    private LocalTime time;

    @jakarta.validation.constraints.DecimalMin(value = "0.00", message = "Цената не може да бъде отрицателна.")
    private BigDecimal price = BigDecimal.ZERO;

    @Min(value = 1, message = "Капацитетът трябва да бъде поне 1.")
    private int capacity;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.DRAFT;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public Event() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
    public void softDelete(String by) { this.deletedAt = LocalDateTime.now(); this.deletedBy = by; }
    public void restore() { this.deletedAt = null; this.deletedBy = null; }
}
