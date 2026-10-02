package com.example.hefest.controller;

import com.example.hefest.model.RegistrationForm;
import com.example.hefest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String registered,
                        Model model) {
        model.addAttribute("loginError", error != null);
        model.addAttribute("registered", registered != null);
        return "login";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult) {
        if (!Objects.equals(form.getPassword(), form.getPasswordConfirmation())) {
            bindingResult.rejectValue("passwordConfirmation", "password.mismatch",
                    "Паролите не съвпадат.");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(form);
        } catch (IllegalStateException exception) {
            bindingResult.rejectValue("email", "email.duplicate", exception.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
}
