
package com.example.hefest.repository;

import com.example.hefest.model.ExerciseRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExerciseRecordQueryDao {

    private final JdbcTemplate jdbcTemplate;
    private final ExerciseRecordRowMapper rowMapper;

    public ExerciseRecordQueryDao(
            JdbcTemplate jdbcTemplate,
            ExerciseRecordRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    public List<ExerciseRecord> findAll() {
        String sql = """
                SELECT id, title, created_at
                FROM ddl_exercise
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }
}
