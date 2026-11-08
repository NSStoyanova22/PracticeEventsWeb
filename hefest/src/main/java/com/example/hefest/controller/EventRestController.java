package com.example.hefest.controller;

import com.example.hefest.model.Event;
import com.example.hefest.model.User;
import com.example.hefest.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/events")
public class EventRestController {
    public record EventDto(@NotBlank String title, @NotBlank String description, @NotNull LocalDate date,
                           LocalTime time, @NotNull @DecimalMin("0.00") BigDecimal price, @Min(1) int capacity,
                           Long version) {
        Event toEntity() { Event event = new Event(); event.setTitle(title.trim()); event.setDescription(description.trim()); event.setDate(date); event.setTime(time); event.setPrice(price); event.setCapacity(capacity); event.setVersion(version); return event; }
    }
    public record EventResponse(Long id, String title, String description, LocalDate date, LocalTime time, BigDecimal price, int capacity, String status, Long version) {
        static EventResponse from(Event e) { return new EventResponse(e.getId(), e.getTitle(), e.getDescription(), e.getDate(), e.getTime(), e.getPrice(), e.getCapacity(), e.getStatus().name(), e.getVersion()); }
    }
    private final EventService eventService;
    public EventRestController(EventService eventService) { this.eventService = eventService; }

    @GetMapping
    public Page<EventResponse> list(@RequestParam(defaultValue = "") String title,
                                    @RequestParam(required = false) LocalDate fromDate,
                                    @RequestParam(required = false) LocalDate toDate,
                                    @RequestParam(required = false) BigDecimal minPrice,
                                    @RequestParam(required = false) BigDecimal maxPrice,
                                    @RequestParam(required = false) Integer minCapacity,
                                    @RequestParam(defaultValue = "date") String sort,
                                    @RequestParam(defaultValue = "asc") String direction,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return eventService.searchEvents(title, fromDate, toDate, minPrice, maxPrice, minCapacity, sort, direction, page, size).map(EventResponse::from);
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) { return EventResponse.from(eventService.getEvent(id)); }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventDto dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(EventResponse.from(eventService.createEvent(dto.toEntity(), user)));
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventDto dto, @AuthenticationPrincipal User user) {
        return EventResponse.from(eventService.updateEvent(id, dto.toEntity(), user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        eventService.deleteEvent(id, user); return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<java.util.Map<String, String>> badRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(java.util.Map.of("error", exception.getMessage()));
    }
}
