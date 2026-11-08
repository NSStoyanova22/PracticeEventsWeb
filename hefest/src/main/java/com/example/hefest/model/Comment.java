package com.example.hefest.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "event_id") private Event event;
    @ManyToOne(optional = false) @JoinColumn(name = "author_id") private User author;
    private Long parentId;
    @Column(columnDefinition = "TEXT", nullable = false) private String content;
    private boolean deleted;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt;
    protected Comment() { }
    public Comment(Event event, User author, Long parentId, String content) { this.event = event; this.author = author; this.parentId = parentId; this.content = content; }
    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public User getAuthor() { return author; }
    public Long getParentId() { return parentId; }
    public String getContent() { return content; }
    public void edit(String content) { this.content = content; this.editedAt = LocalDateTime.now(); }
    public boolean isDeleted() { return deleted; }
    public void moderateDelete() { this.deleted = true; this.content = "[Коментарът е изтрит от модератор]"; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getEditedAt() { return editedAt; }
}
