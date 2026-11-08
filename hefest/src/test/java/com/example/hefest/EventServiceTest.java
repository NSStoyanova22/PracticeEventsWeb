package com.example.hefest;

import com.example.hefest.model.Event;
import com.example.hefest.repository.EventRepository;
import com.example.hefest.service.AuditLogService;
import com.example.hefest.service.EventService;
import com.example.hefest.service.EventVersionService;
import com.example.hefest.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock EventRepository eventRepository;
    @Mock AuditLogService auditLogService;
    @Mock NotificationService notificationService;
    @Mock EventVersionService versionService;
    @InjectMocks EventService eventService;

    @Test
    void updateSavesWhenVersionIsCurrent() {
        Event stored = event(1L, 3L, "Old");
        Event form = event(null, 3L, "New");
        when(eventRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(stored));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(1L, form, null);

        assertEquals("New", result.getTitle());
        verify(eventRepository).save(stored);
        verify(versionService).saveVersion(stored, null);
    }

    @Test
    void updateRejectsStaleVersion() {
        Event stored = event(1L, 4L, "Current");
        Event staleForm = event(null, 3L, "Old edit");
        when(eventRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(stored));

        assertThrows(EventService.ConcurrentEditException.class,
                () -> eventService.updateEvent(1L, staleForm, null));
        verify(eventRepository, never()).save(any(Event.class));
    }

    private static Event event(Long id, Long version, String title) {
        Event event = new Event(); event.setId(id); event.setVersion(version); event.setTitle(title);
        event.setDescription("Description"); event.setDate(LocalDate.of(2026, 1, 1));
        event.setPrice(BigDecimal.ZERO); event.setCapacity(10); return event;
    }
}
