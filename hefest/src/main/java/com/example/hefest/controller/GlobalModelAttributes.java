package com.example.hefest.controller;

import com.example.hefest.model.Role;
import com.example.hefest.model.User;
import com.example.hefest.service.NotificationService;
import com.example.hefest.service.UserPreferencesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalModelAttributes {

    private final NotificationService notificationService;
    private final UserPreferencesService preferencesService;

    public GlobalModelAttributes(NotificationService notificationService, UserPreferencesService preferencesService) {
        this.notificationService = notificationService;
        this.preferencesService = preferencesService;
    }

    @ModelAttribute
    public void addCurrentUser(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("isAdmin", currentUser != null && currentUser.getRole() == Role.ADMIN);
        model.addAttribute("canEditLibrary", currentUser != null
                && (currentUser.getRole() == Role.ADMIN));
        model.addAttribute("canManageEvents", currentUser != null
                && (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.EDITOR));
        model.addAttribute("unreadNotifications", currentUser == null ? 0 : notificationService.unreadCount(currentUser));
        if (currentUser != null) {
            var preferences = preferencesService.getOrCreate(currentUser);
            model.addAttribute("userTheme", preferences.getTheme());
            model.addAttribute("userLanguage", preferences.getLanguage());
        } else {
            model.addAttribute("userTheme", "light");
            model.addAttribute("userLanguage", "bg");
        }
    }
}
