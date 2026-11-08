package com.example.hefest.controller;

import com.example.hefest.model.Event;
import com.example.hefest.model.User;
import com.example.hefest.service.EventService;
import com.example.hefest.service.RegistrationService;
import com.example.hefest.service.EventVersionService;
import com.example.hefest.service.WorkflowService;
import com.example.hefest.service.CommentService;
import com.example.hefest.service.CollectionService;
import com.example.hefest.service.UserPreferencesService;
import com.example.hefest.model.EventStatus;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final EventVersionService versionService;
    private final WorkflowService workflowService;
    private final CommentService commentService;
    private final CollectionService collectionService;
    private final UserPreferencesService preferencesService;

    public EventController(EventService eventService, RegistrationService registrationService,
                           EventVersionService versionService, WorkflowService workflowService, CommentService commentService, CollectionService collectionService, UserPreferencesService preferencesService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.versionService = versionService; this.workflowService = workflowService;
        this.commentService = commentService;
        this.collectionService = collectionService;
        this.preferencesService = preferencesService;
    }

    @GetMapping
    public String events(@RequestParam(defaultValue = "") String title,
                         @RequestParam(required = false) java.time.LocalDate fromDate,
                         @RequestParam(required = false) java.time.LocalDate toDate,
                         @RequestParam(required = false) java.math.BigDecimal minPrice,
                         @RequestParam(required = false) java.math.BigDecimal maxPrice,
                         @RequestParam(required = false) Integer minCapacity,
                         @RequestParam(defaultValue = "date") String sort,
                         @RequestParam(defaultValue = "asc") String direction,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) Integer size,
                         @AuthenticationPrincipal User currentUser,
                         Model model) {
        int effectiveSize = size == null ? (currentUser == null ? 9 : preferencesService.getOrCreate(currentUser).getPageSize()) : size;
        var eventPage = eventService.searchEvents(title, fromDate, toDate, minPrice, maxPrice, minCapacity,
                sort, direction, page, effectiveSize);
        List<Event> events = eventPage.getContent();
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
        model.addAttribute("eventPage", eventPage);
        model.addAttribute("title", title);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minCapacity", minCapacity);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("size", effectiveSize);

        return "events";
    }

    @GetMapping("/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("isEdit", false);
        return "event-form";
    }

    @PostMapping
    public String createEvent(@Valid @ModelAttribute("event") Event event,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal User currentUser) {
        if (bindingResult.hasErrors()) {
            return "event-form";
        }

        eventService.createEvent(event, currentUser);
        return "redirect:/events";
    }

    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEvent(id));
        model.addAttribute("isEdit", true);
        return "event-form";
    }

    @PostMapping("/{id}")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("event") Event event,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal User currentUser,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "event-form";
        }
        try {
            eventService.updateEvent(id, event, currentUser);
        } catch (EventService.ConcurrentEditException exception) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("event", eventService.getEvent(id));
            return "event-form";
        }
        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        eventService.deleteEvent(id, currentUser);
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
        model.addAttribute("versions", versionService.versions(id));
        model.addAttribute("workflowHistory", workflowService.history(id));
        model.addAttribute("comments", commentService.forEvent(id));
        if (currentUser != null) {
            model.addAttribute("isFavorite", collectionService.isFavorite(id, currentUser));
            model.addAttribute("collections", collectionService.collections(currentUser));
        }
        return "event-detail";
    }

    @PostMapping("/{id}/versions/{versionId}/restore")
    public String restoreVersion(@PathVariable Long id, @PathVariable Long versionId,
                                 @AuthenticationPrincipal User currentUser) {
        versionService.restore(id, versionId, currentUser);
        return "redirect:/events/" + id;
    }

    @GetMapping("/{id}/versions/compare")
    public String compareVersions(@PathVariable Long id,
                                  @RequestParam(required = false) Long left,
                                  @RequestParam(required = false) Long right,
                                  Model model) {
        model.addAttribute("event", eventService.getEvent(id));
        model.addAttribute("versions", versionService.versions(id));
        if (left != null && right != null) {
            model.addAttribute("leftVersion", versionService.get(id, left));
            model.addAttribute("rightVersion", versionService.get(id, right));
        }
        return "event-version-compare";
    }

    @PostMapping("/{id}/status")
    public String transitionStatus(@PathVariable Long id, @RequestParam EventStatus status,
                                   @AuthenticationPrincipal User currentUser) {
        workflowService.transition(id, status, currentUser);
        return "redirect:/events/" + id;
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
