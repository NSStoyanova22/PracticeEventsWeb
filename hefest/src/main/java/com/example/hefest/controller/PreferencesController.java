package com.example.hefest.controller;

import com.example.hefest.model.User;
import com.example.hefest.service.UserPreferencesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PreferencesController {
    private final UserPreferencesService service;
    public PreferencesController(UserPreferencesService service) { this.service = service; }

    @GetMapping("/settings")
    public String form(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("preferences", service.getOrCreate(user));
        return "settings";
    }

    @PostMapping("/settings")
    public String save(@AuthenticationPrincipal User user, @RequestParam String language,
                       @RequestParam String theme, @RequestParam int pageSize,
                       @RequestParam(defaultValue = "false") boolean emailNotifications,
                       RedirectAttributes redirect) {
        service.update(user, language, theme, pageSize, emailNotifications);
        redirect.addFlashAttribute("successMessage", "Настройките са запазени.");
        return "redirect:/settings";
    }
}
