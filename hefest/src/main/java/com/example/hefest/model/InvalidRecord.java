package com.example.hefest.model;

import java.util.List;

/**
 * A database row that breaks at least one rule of the application.
 *
 * @param table    table the row lives in
 * @param recordId primary key of the row
 * @param label    human-readable name of the row (title, e-mail, ...)
 * @param problems what is wrong with it
 */
public record InvalidRecord(String table, Long recordId, String label, List<String> problems) {
}
