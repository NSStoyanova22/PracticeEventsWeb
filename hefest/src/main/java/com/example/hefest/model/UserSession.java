package com.example.hefest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** One login of a user: from successful sign-in until logout, timeout or server restart. */
@Entity
@Table(name = "user_session", indexes = @Index(name = "idx_user_session_hash", columnList = "sessionIdHash"))
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /** SHA-256 of the HTTP session id; the raw id is never stored. */
    @Column(length = 64, nullable = false)
    private String sessionIdHash;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    private LocalDateTime loginAt = LocalDateTime.now();

    private LocalDateTime logoutAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SessionEndReason endReason;

    public UserSession() {
    }

    public UserSession(User user, String sessionIdHash, String ipAddress, String userAgent) {
        this.user = user;
        this.sessionIdHash = sessionIdHash;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public void end(SessionEndReason reason) {
        this.logoutAt = LocalDateTime.now();
        this.endReason = reason;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getSessionIdHash() { return sessionIdHash; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public LocalDateTime getLoginAt() { return loginAt; }
    public LocalDateTime getLogoutAt() { return logoutAt; }
    public SessionEndReason getEndReason() { return endReason; }
}
