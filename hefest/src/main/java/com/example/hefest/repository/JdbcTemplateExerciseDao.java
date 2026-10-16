
package com.example.hefest.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTemplateExerciseDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateExerciseDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insertExercise(String name, String description) {
        String sql = """
                INSERT INTO db_exercise (name, description)
                VALUES (?, ?)
                """;

        return jdbcTemplate.update(sql, name, description);
    }

    public int updateExercise(
            String oldName, String newName, String description) {

        String sql = """
                UPDATE db_exercise
                SET name = ?, description = ?
                WHERE name = ?
                """;

        return jdbcTemplate.update(
                sql, newName, description, oldName);
    }

    public int deleteExercise(String name) {
        String sql = """
                DELETE FROM db_exercise
                WHERE name = ?
                """;

        return jdbcTemplate.update(sql, name);
    }
}
