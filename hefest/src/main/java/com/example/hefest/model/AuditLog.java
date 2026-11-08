package com.example.hefest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(nullable = false, length = 80)
    private String entityType;

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String oldValues;

    @Column(columnDefinition = "TEXT")
    private String newValues;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected AuditLog() { }

    public AuditLog(User actor, String action, String entityType, Long entityId,
                    String oldValues, String newValues) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValues = oldValues;
        this.newValues = newValues;
    }

    public Long getId() { return id; }
    public User getActor() { return actor; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getOldValues() { return oldValues; }
    public String getNewValues() { return newValues; }
    public LocalDateTime getCreatedAt() { return createdAt; }

}
