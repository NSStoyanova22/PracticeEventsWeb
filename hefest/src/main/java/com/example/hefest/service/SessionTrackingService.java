package com.example.hefest.service;

import com.example.hefest.model.SessionEndReason;
import com.example.hefest.model.User;
import com.example.hefest.model.UserSession;
import com.example.hefest.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

@Service
public class SessionTrackingService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public record SessionView(Long id, String userName, String userEmail, String ipAddress,
                              String browser, String userAgent, String loginAt, String logoutAt,
                              String duration, String endReason) {
    }

    private final UserSessionRepository userSessionRepository;

    public SessionTrackingService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public void recordLogin(User user, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 255) {
            userAgent = userAgent.substring(0, 255);
        }
        String sessionId = request.getSession().getId();
        userSessionRepository.save(new UserSession(user, hash(sessionId), request.getRemoteAddr(), userAgent));
    }

    @Transactional
    public void endSession(String sessionId, SessionEndReason reason) {
        if (sessionId == null) {
            return;
        }
        userSessionRepository.findFirstBySessionIdHashAndLogoutAtIsNull(hash(sessionId))
                .ifPresent(session -> {
                    session.end(reason);
                    userSessionRepository.save(session);
                });
    }

    /** Sessions live in memory, so none survive a restart: close whatever was left open. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void closeSessionsLeftOpen() {
        userSessionRepository.endAllOpen(LocalDateTime.now(), SessionEndReason.SERVER_RESTART);
    }

    @Transactional(readOnly = true)
    public Page<SessionView> pastSessions(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return userSessionRepository
                .findByLogoutAtIsNotNullOrderByLogoutAtDesc(PageRequest.of(Math.max(page, 0), safeSize))
                .map(SessionTrackingService::toView);
    }

    public long activeCount() {
        return userSessionRepository.countByLogoutAtIsNull();
    }

    public long pastCount() {
        return userSessionRepository.countByLogoutAtIsNotNull();
    }

    private static SessionView toView(UserSession s) {
        return new SessionView(
                s.getId(),
                s.getUser().getName(),
                s.getUser().getEmail(),
                displayIp(s.getIpAddress()),
                browserOf(s.getUserAgent()),
                s.getUserAgent(),
                s.getLoginAt() == null ? "—" : DATE_TIME.format(s.getLoginAt()),
                s.getLogoutAt() == null ? "—" : DATE_TIME.format(s.getLogoutAt()),
                durationOf(s.getLoginAt(), s.getLogoutAt()),
                reasonLabel(s.getEndReason()));
    }

    private static String displayIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "—";
        }
        return switch (ip) {
            case "127.0.0.1", "0:0:0:0:0:0:0:1", "::1" -> "localhost";
            default -> ip;
        };
    }

    private static String reasonLabel(SessionEndReason reason) {
        if (reason == null) {
            return "—";
        }
        return switch (reason) {
            case LOGOUT -> "Изход";
            case EXPIRED -> "Изтекла";
            case SERVER_RESTART -> "Рестарт на сървъра";
        };
    }

    private static String durationOf(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return "—";
        }
        long minutes = Duration.between(from, to).toMinutes();
        if (minutes < 1) {
            return "под 1 мин";
        }
        if (minutes < 60) {
            return minutes + " мин";
        }
        return (minutes / 60) + " ч " + (minutes % 60) + " мин";
    }

    /** Short "Browser · OS" label from a User-Agent header. */
    static String browserOf(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Неизвестен";
        }
        String browser;
        if (userAgent.contains("Edg/")) browser = "Edge";
        else if (userAgent.contains("OPR/")) browser = "Opera";
        else if (userAgent.contains("Firefox/")) browser = "Firefox";
        else if (userAgent.contains("Chrome/")) browser = "Chrome";
        else if (userAgent.contains("Safari/")) browser = "Safari";
        else if (userAgent.startsWith("curl/")) browser = "curl";
        else browser = "Друг";

        String os;
        if (userAgent.contains("Windows")) os = "Windows";
        else if (userAgent.contains("Android")) os = "Android";
        else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";
        else if (userAgent.contains("Mac OS")) os = "macOS";
        else if (userAgent.contains("Linux")) os = "Linux";
        else return browser;

        return browser + " · " + os;
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
