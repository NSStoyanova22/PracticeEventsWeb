package com.example.hefest.controller;

import com.example.hefest.model.User;
import com.example.hefest.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** JSON endpoints polled by chat.js (users) and admin-chat.js (admins). */
@RestController
public class ChatApiController {

    public record NewMessage(String content) {
    }

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    // --- user side ---

    @GetMapping("/chat/messages")
    public List<ChatService.MessageView> myMessages(@AuthenticationPrincipal User currentUser,
                                                    @RequestParam(defaultValue = "0") long after) {
        return chatService.getUserMessages(currentUser, after);
    }

    @PostMapping("/chat/messages")
    public ChatService.MessageView sendMessage(@AuthenticationPrincipal User currentUser,
                                               @RequestBody NewMessage body) {
        return chatService.postUserMessage(currentUser, body.content());
    }

    // --- admin side ---

    @GetMapping("/admin/chat/api/conversations")
    public List<ChatService.ConversationView> conversations() {
        return chatService.listConversations();
    }

    @GetMapping("/admin/chat/api/waiting")
    public Map<String, Long> waiting() {
        return Map.of("count", chatService.waitingCount());
    }

    @GetMapping("/admin/chat/api/conversations/{id}/messages")
    public List<ChatService.MessageView> conversationMessages(@PathVariable Long id,
                                                              @RequestParam(defaultValue = "0") long after) {
        return chatService.getConversationMessages(id, after);
    }

    @PostMapping("/admin/chat/api/conversations/{id}/messages")
    public ChatService.MessageView reply(@AuthenticationPrincipal User currentUser,
                                         @PathVariable Long id,
                                         @RequestBody NewMessage body) {
        return chatService.postAdminMessage(currentUser, id, body.content());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
