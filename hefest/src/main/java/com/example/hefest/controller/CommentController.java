package com.example.hefest.controller;

import com.example.hefest.model.User;
import com.example.hefest.service.CommentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {
    private final CommentService commentService;
    public CommentController(CommentService commentService) { this.commentService = commentService; }
    @PostMapping("/events/{eventId}/comments")
    public String add(@PathVariable Long eventId, @RequestParam(required = false) Long parentId,
                      @RequestParam String content, @AuthenticationPrincipal User user, RedirectAttributes redirect) {
        try { commentService.add(eventId, parentId, content, user); } catch (RuntimeException e) { redirect.addFlashAttribute("commentError", e.getMessage()); }
        return "redirect:/events/" + eventId;
    }
    @PostMapping("/comments/{id}/edit")
    public String edit(@PathVariable Long id, @RequestParam String content, @AuthenticationPrincipal User user) {
        Long eventId = commentService.eventId(id); try { commentService.edit(id, content, user); } catch (RuntimeException ignored) { }
        return "redirect:/events/" + eventId;
    }
    @PostMapping("/comments/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Long eventId = commentService.eventId(id); try { commentService.delete(id, user); } catch (RuntimeException ignored) { }
        return "redirect:/events/" + eventId;
    }
}
