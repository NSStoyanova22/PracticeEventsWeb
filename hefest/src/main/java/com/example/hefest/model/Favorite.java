package com.example.hefest.model;
import jakarta.persistence.*;
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
public class Favorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name="user_id") private User user;
    @ManyToOne(optional = false) @JoinColumn(name="event_id") private Event event;
    protected Favorite() { }
    public Favorite(User user, Event event) { this.user = user; this.event = event; }
    public Long getId() { return id; } public Event getEvent() { return event; }
}
