package com.example.hefest.repository;

import com.example.hefest.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationRepository
        extends JpaRepository<Registration, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    long countByEventId(Long eventId);

    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);
    
}
