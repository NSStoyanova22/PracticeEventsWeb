package com.example.hefest.service;

import com.example.hefest.model.Event;
import com.example.hefest.repository.EventRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow();
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }
    public List<Event> getSortedEvents() {
        return eventRepository.findAll().stream()
                .sorted(Comparator.comparing(Event::getId))
                .toList();
    }
}
