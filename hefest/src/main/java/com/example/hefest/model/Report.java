package com.example.hefest.model;

import java.sql.Date;

public class Report {
    Date from;
    Date to;
    int numberOfEvents;
    int numberOfRegistered;
    Event mostPopularEvent;

    public Report(Date from, Date to, int numberOfEvents, int numberOfRegistered, Event mostPopularEvent) {
        this.from = from;
        this.to = to;
        this.numberOfEvents = numberOfEvents;
        this.numberOfRegistered = numberOfRegistered;
        this.mostPopularEvent = mostPopularEvent;
    }
}
