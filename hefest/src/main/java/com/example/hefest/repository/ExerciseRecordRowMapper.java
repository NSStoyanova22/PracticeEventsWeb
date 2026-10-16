
package com.example.hefest.repository;

import com.example.hefest.model.ExerciseRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ExerciseRecordRowMapper implements RowMapper<ExerciseRecord> {

    @Override
    public ExerciseRecord mapRow(ResultSet rs, int rowNum)
            throws SQLException {

        return new ExerciseRecord(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getDate("created_at") == null
                        ? null
                        : rs.getDate("created_at").toLocalDate()
        );
    }
}
