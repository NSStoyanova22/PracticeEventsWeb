package com.example.hefest.controller;

import com.example.hefest.model.User;
import com.example.hefest.service.EventImportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class EventImportController {
    private static final String SESSION_KEY = "eventImportPreview";
    private final EventImportService importService;

    public EventImportController(EventImportService importService) { this.importService = importService; }

    @GetMapping("/events/import")
    public String form() { return "event-import"; }

    @PostMapping("/events/import/preview")
    public String preview(@RequestParam MultipartFile file, HttpSession session, Model model) {
        try {
            EventImportService.Preview preview = importService.preview(file);
            session.setAttribute(SESSION_KEY, preview);
            model.addAttribute("preview", preview);
        } catch (IllegalArgumentException e) { model.addAttribute("errorMessage", e.getMessage()); }
        return "event-import";
    }

    @PostMapping("/events/import/commit")
    public String commit(HttpSession session, @AuthenticationPrincipal User user, Model model) {
        try {
            EventImportService.Preview preview = (EventImportService.Preview) session.getAttribute(SESSION_KEY);
            importService.commit(preview, user);
            session.removeAttribute(SESSION_KEY);
            return "redirect:/events";
        } catch (IllegalStateException e) { model.addAttribute("errorMessage", e.getMessage()); return "event-import"; }
    }
}
