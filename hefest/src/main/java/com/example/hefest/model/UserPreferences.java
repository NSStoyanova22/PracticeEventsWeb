package com.example.hefest.model;

import jakarta.persistence.*;

@Entity
public class UserPreferences {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "user_id", unique = true) private User user;
    private String language = "bg";
    private String theme = "light";
    private int pageSize = 9;
    private boolean emailNotifications = true;

    protected UserPreferences() { }
    public UserPreferences(User user) { this.user = user; }
    public Long getId() { return id; } public User getUser() { return user; }
    public String getLanguage() { return language; } public void setLanguage(String language) { this.language = language; }
    public String getTheme() { return theme; } public void setTheme(String theme) { this.theme = theme; }
    public int getPageSize() { return pageSize; } public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public boolean isEmailNotifications() { return emailNotifications; } public void setEmailNotifications(boolean value) { this.emailNotifications = value; }
}
