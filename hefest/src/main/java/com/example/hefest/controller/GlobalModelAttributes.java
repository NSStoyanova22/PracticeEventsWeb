package com.example.hefest.controller;

import com.example.hefest.model.Role;
import com.example.hefest.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addCurrentUser(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("isAdmin", currentUser != null && currentUser.getRole() == Role.ADMIN);
        model.addAttribute("canEditLibrary", currentUser != null
                && (currentUser.getRole() == Role.ADMIN));
    }
}
