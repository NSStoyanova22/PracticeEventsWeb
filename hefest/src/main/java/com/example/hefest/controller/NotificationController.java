package com.example.hefest.controller;

import com.example.hefest.model.User;
import com.example.hefest.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("notifications", notificationService.forUser(user));
        return "notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notificationService.markRead(id, user);
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(@AuthenticationPrincipal User user) {
        notificationService.markAllRead(user);
        return "redirect:/notifications";
    }
}
