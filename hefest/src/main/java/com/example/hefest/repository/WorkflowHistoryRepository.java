package com.example.hefest.repository;

import com.example.hefest.model.WorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {
    List<WorkflowHistory> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
