
package com.example.hefest.model;

import java.time.LocalDate;

public class ExerciseRecord {

    private final long id;
    private final String title;
    private final LocalDate createdAt;

    public ExerciseRecord(long id, String title, LocalDate createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
