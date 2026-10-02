package com.example.hefest.controller;

import com.example.hefest.model.Event;
import com.example.hefest.model.User;
import com.example.hefest.service.EventService;
import com.example.hefest.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;

    public EventController(EventService eventService, RegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @GetMapping
    public String events(Model model) {

        List<Event> events = eventService.getAllEvents();
        Map<Long, Long> registeredCounts = new HashMap<>();
        Map<Long, Long> availablePlaces = new HashMap<>();
        Map<Long, Long> capacityPercent = new HashMap<>();

        for (Event event : events) {
            long registered = registrationService.countForEvent(event.getId());
            registeredCounts.put(event.getId(), registered);
            availablePlaces.put(event.getId(), Math.max(0, event.getCapacity() - registered));
            long percent = event.getCapacity() > 0
                    ? Math.min(100, registered * 100 / event.getCapacity())
                    : 0;
            capacityPercent.put(event.getId(), percent);
        }

        model.addAttribute("events", events);
        model.addAttribute("registeredCounts", registeredCounts);
        model.addAttribute("availablePlaces", availablePlaces);
        model.addAttribute("capacityPercent", capacityPercent);

        return "events";
    }

    @GetMapping("/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "event-form";
    }

    @PostMapping
    public String createEvent(@Valid @ModelAttribute("event") Event event,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "event-form";
        }

        eventService.createEvent(event);
        return "redirect:/events";
    }

    @GetMapping("/{id}")
    public String eventDetails(@PathVariable Long id,
                               @AuthenticationPrincipal User currentUser,
                               Model model) {
        Event event = eventService.getEvent(id);
        long registeredCount = registrationService.countForEvent(id);
        model.addAttribute("event", event);
        model.addAttribute("registeredCount", registeredCount);
        model.addAttribute("availablePlaces", Math.max(0, event.getCapacity() - registeredCount));
        model.addAttribute("capacityPercent", event.getCapacity() > 0
                ? Math.min(100, registeredCount * 100 / event.getCapacity())
                : 0);
        model.addAttribute("currentUserRegistered", registrationService.isRegistered(id, currentUser));
        return "event-detail";
    }
    @GetMapping("/sorted")
    public String sortedEvents(Model model) {
    List<Event> events = eventService.getSortedEvents();

    Map<Long, Long> registeredCounts = new HashMap<>();
    Map<Long, Long> availablePlaces = new HashMap<>();
    Map<Long, Long> capacityPercent = new HashMap<>();

    for (Event event : events) {
        long registered = registrationService.countForEvent(event.getId());

        registeredCounts.put(event.getId(), registered);
        availablePlaces.put(
                event.getId(),
                Math.max(0, event.getCapacity() - registered)
        );

        long percent = event.getCapacity() > 0
                ? Math.min(100, registered * 100 / event.getCapacity())
                : 0;

        capacityPercent.put(event.getId(), percent);
    }

    model.addAttribute("events", events);
    model.addAttribute("registeredCounts", registeredCounts);
    model.addAttribute("availablePlaces", availablePlaces);
    model.addAttribute("capacityPercent", capacityPercent);

    return "sorted-events";
    }
}
