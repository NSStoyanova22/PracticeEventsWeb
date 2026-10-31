package com.example.hefest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminChatController {

    @GetMapping("/admin/chat")
    public String chat() {
        return "admin/chat";
    }
}
