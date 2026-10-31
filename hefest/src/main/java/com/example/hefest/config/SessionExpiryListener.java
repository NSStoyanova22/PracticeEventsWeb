package com.example.hefest.config;

import com.example.hefest.model.SessionEndReason;
import com.example.hefest.service.SessionTrackingService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

/**
 * Closes the stored login session when the HTTP session is destroyed. After a logout the row is
 * already closed with LOGOUT, so this only marks sessions that timed out.
 */
@Component
public class SessionExpiryListener implements HttpSessionListener {

    private final SessionTrackingService sessionTrackingService;

    public SessionExpiryListener(SessionTrackingService sessionTrackingService) {
        this.sessionTrackingService = sessionTrackingService;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        sessionTrackingService.endSession(event.getSession().getId(), SessionEndReason.EXPIRED);
    }
}
