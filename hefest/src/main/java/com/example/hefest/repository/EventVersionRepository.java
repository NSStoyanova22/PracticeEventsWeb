package com.example.hefest.repository;

import com.example.hefest.model.EventVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EventVersionRepository extends JpaRepository<EventVersion, Long> {
    List<EventVersion> findByEventIdOrderByVersionNumberDesc(Long eventId);
    Optional<EventVersion> findByIdAndEventId(Long id, Long eventId);
    int countByEventId(Long eventId);
}
