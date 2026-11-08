package com.example.hefest.service;

import com.example.hefest.model.User;
import com.example.hefest.model.UserPreferences;
import com.example.hefest.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferencesService {
    private final UserPreferencesRepository repository;
    public UserPreferencesService(UserPreferencesRepository repository) { this.repository = repository; }

    @Transactional
    public UserPreferences getOrCreate(User user) {
        return repository.findByUserId(user.getId()).orElseGet(() -> repository.save(new UserPreferences(user)));
    }

    @Transactional
    public UserPreferences update(User user, String language, String theme, int pageSize, boolean emailNotifications) {
        UserPreferences preferences = getOrCreate(user);
        preferences.setLanguage("en".equalsIgnoreCase(language) ? "en" : "bg");
        preferences.setTheme("dark".equalsIgnoreCase(theme) ? "dark" : "light");
        preferences.setPageSize(Math.min(Math.max(pageSize, 3), 50));
        preferences.setEmailNotifications(emailNotifications);
        return repository.save(preferences);
    }
}
