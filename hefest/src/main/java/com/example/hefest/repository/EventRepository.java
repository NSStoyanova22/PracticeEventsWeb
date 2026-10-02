package com.example.hefest.repository;

import com.example.hefest.model.Event;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository
        extends JpaRepository<Event, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from Event event where event.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
    List<Event> findByDate(LocalDate date);
    List<Event> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
