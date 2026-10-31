package com.example.hefest.repository;

import com.example.hefest.model.SessionEndReason;
import com.example.hefest.model.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository
        extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findFirstBySessionIdHashAndLogoutAtIsNull(String sessionIdHash);

    Page<UserSession> findByLogoutAtIsNotNullOrderByLogoutAtDesc(Pageable pageable);

    long countByLogoutAtIsNull();

    long countByLogoutAtIsNotNull();

    @Modifying
    @Query("update UserSession s set s.logoutAt = :now, s.endReason = :reason where s.logoutAt is null")
    int endAllOpen(@Param("now") LocalDateTime now, @Param("reason") SessionEndReason reason);
}
