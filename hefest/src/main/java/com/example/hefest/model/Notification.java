package com.example.hefest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String referenceKey;

    @jakarta.persistence.Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private boolean read;
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Notification() { }

    public Notification(User user, String title, String message) {
        this(user, title, message, null);
    }

    public Notification(User user, String title, String message, String referenceKey) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.referenceKey = referenceKey;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getReferenceKey() { return referenceKey; }
    public boolean isRead() { return read; }
    public void markRead() { this.read = true; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
