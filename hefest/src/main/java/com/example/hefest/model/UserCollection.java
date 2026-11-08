package com.example.hefest.model;
import jakarta.persistence.*;
@Entity
@Table(name="user_collection", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
public class UserCollection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name="user_id") private User user;
    @Column(nullable=false) private String name;
    protected UserCollection() { }
    public UserCollection(User user, String name) { this.user = user; this.name = name; }
    public Long getId() { return id; } public String getName() { return name; } public User getUser() { return user; }
}
