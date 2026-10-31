package com.example.hefest.model;

public enum SessionEndReason {
    /** The user clicked "Изход". */
    LOGOUT,
    /** The HTTP session timed out from inactivity. */
    EXPIRED,
    /** The server stopped while the session was open. */
    SERVER_RESTART
}
