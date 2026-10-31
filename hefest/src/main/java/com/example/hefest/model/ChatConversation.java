package com.example.hefest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.time.LocalDateTime;

/** One support thread per user; admins answer in it. */
@Entity
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    /** True when the last message is from the user and no admin has answered yet. */
    private boolean waitingForAdmin;

    @Column(length = 200)
    private String lastMessagePreview;

    private LocalDateTime lastMessageAt = LocalDateTime.now();

    public ChatConversation() {
    }

    public ChatConversation(User user) {
        this.user = user;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public boolean isWaitingForAdmin() { return waitingForAdmin; }
    public void setWaitingForAdmin(boolean waitingForAdmin) { this.waitingForAdmin = waitingForAdmin; }
    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) { this.lastMessagePreview = lastMessagePreview; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
