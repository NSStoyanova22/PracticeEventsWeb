package com.example.hefest.controller;

import com.example.hefest.model.User;
import com.example.hefest.service.RegistrationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{eventId}/registrations")
    public String register(@PathVariable Long eventId,
                           @AuthenticationPrincipal User currentUser,
                           RedirectAttributes redirectAttributes) {
        try {
            registrationService.register(eventId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Успешно се регистрирахте за събитието.");
        } catch (RegistrationService.DuplicateRegistrationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Този потребител вече е регистриран за събитието.");
        } catch (RegistrationService.EventFullException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Събитието е пълно.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Събитието не беше намерено.");
        }

        return "redirect:/events/" + eventId;
    }

    @PostMapping("/{eventId}/registrations/cancel")
    public String cancel(@PathVariable Long eventId,
                         @AuthenticationPrincipal User currentUser,
                         RedirectAttributes redirectAttributes) {
        try {
            registrationService.cancel(eventId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Регистрацията беше отменена.");
        } catch (IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Няма активна регистрация за това събитие.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Събитието не беше намерено.");
        }

        return "redirect:/events/" + eventId;
    }
}
