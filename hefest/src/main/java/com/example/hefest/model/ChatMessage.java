package com.example.hefest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conversation_id")
    private ChatConversation conversation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    private boolean fromAdmin;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage() {
    }

    public ChatMessage(ChatConversation conversation, User sender, boolean fromAdmin, String content) {
        this.conversation = conversation;
        this.sender = sender;
        this.fromAdmin = fromAdmin;
        this.content = content;
    }

    public Long getId() { return id; }
    public ChatConversation getConversation() { return conversation; }
    public User getSender() { return sender; }
    public boolean isFromAdmin() { return fromAdmin; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
