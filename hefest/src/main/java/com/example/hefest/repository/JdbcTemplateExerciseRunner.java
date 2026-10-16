
package com.example.hefest.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JdbcTemplateExerciseRunner
        implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(JdbcTemplateExerciseRunner.class);

    private final JdbcTemplateExerciseDao dao;
    private final JdbcTemplateDdlExercise ddlExercise;
    private final ExerciseRecordQueryDao queryDao;

    public JdbcTemplateExerciseRunner(JdbcTemplateExerciseDao dao, JdbcTemplateDdlExercise ddlExercise, ExerciseRecordQueryDao queryDao) {
        this.dao = dao;
        this.ddlExercise = ddlExercise;
        this.queryDao = queryDao;
    }

    @Override
    public void run(String... args) {
        ddlExercise.createExerciseTable();
        log.info("DDL CREATE TABLE executed successfully");
        String originalName = "JDBC Exercise Demo";
        String updatedName = "JDBC Exercise Updated";

        int inserted = dao.insertExercise(
                originalName, "Record created with JdbcTemplate");

        log.info("INSERT affected rows: {}", inserted);

        int updated = dao.updateExercise(
                originalName,
                updatedName,
                "Record updated with JdbcTemplate");

        log.info("UPDATE affected rows: {}", updated);

        int deleted = dao.deleteExercise(updatedName);

        log.info("DELETE affected rows: {}", deleted);
        queryDao.findAll().forEach(record ->
                log.info(
                        "ID: {}, Title: {}, Created at: {}",
                        record.getId(),
                        record.getTitle(),
                        record.getCreatedAt()
                )
        );
    }
}
