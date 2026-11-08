package com.example.hefest.controller;

import com.example.hefest.model.User;
import com.example.hefest.service.EventService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/trash")
public class TrashController {
    private final EventService eventService;
    public TrashController(EventService eventService) { this.eventService = eventService; }
    @GetMapping public String trash(Model model) { model.addAttribute("deletedEvents", eventService.deletedEvents()); return "admin/trash"; }
    @PostMapping("/{id}/restore") public String restore(@PathVariable Long id, @AuthenticationPrincipal User user) { eventService.restoreEvent(id, user); return "redirect:/admin/trash"; }
}
