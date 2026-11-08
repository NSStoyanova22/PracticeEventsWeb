package com.example.hefest.repository;

import com.example.hefest.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndDeletedFalseOrderByCreatedAtAsc(Long eventId);
}
