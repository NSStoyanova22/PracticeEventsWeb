package com.example.hefest.repository;

import com.example.hefest.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    long countByEventId(Long eventId);

    /** Rows of [eventId, registrationCount] for every event that has registrations. */
    @Query("select r.event.id, count(r) from Registration r group by r.event.id")
    List<Object[]> countPerEvent();

    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);

}
