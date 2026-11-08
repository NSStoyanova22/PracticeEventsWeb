package com.example.hefest.model;
import jakarta.persistence.*;
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"collection_id", "event_id"}))
public class CollectionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="collection_id") private UserCollection collection;
    @ManyToOne(optional=false) @JoinColumn(name="event_id") private Event event;
    protected CollectionItem() { }
    public CollectionItem(UserCollection collection, Event event) { this.collection = collection; this.event = event; }
    public Long getId() { return id; } public Event getEvent() { return event; }
}
