package com.example.hefest.repository;

import com.example.hefest.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("select a from AuditLog a left join a.actor u " +
            "where (:actor is null or lower(u.email) like lower(concat('%', :actor, '%'))) " +
            "and (:entityType is null or a.entityType = :entityType) " +
            "and (:fromDate is null or a.createdAt >= :fromDate) " +
            "and (:toDate is null or a.createdAt < :toDate) " +
            "order by a.createdAt desc")
    Page<AuditLog> search(@Param("actor") String actor,
                          @Param("entityType") String entityType,
                          @Param("fromDate") LocalDateTime fromDate,
                          @Param("toDate") LocalDateTime toDate,
                          Pageable pageable);
}
