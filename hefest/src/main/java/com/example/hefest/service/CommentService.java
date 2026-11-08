package com.example.hefest.service;

import com.example.hefest.model.Comment;
import com.example.hefest.model.Event;
import com.example.hefest.model.Role;
import com.example.hefest.model.User;
import com.example.hefest.repository.CommentRepository;
import com.example.hefest.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    public CommentService(CommentRepository commentRepository, EventRepository eventRepository) { this.commentRepository = commentRepository; this.eventRepository = eventRepository; }

    @Transactional(readOnly = true)
    public List<Comment> forEvent(Long eventId) { return commentRepository.findByEventIdAndDeletedFalseOrderByCreatedAtAsc(eventId); }

    @Transactional
    public Comment add(Long eventId, Long parentId, String content, User author) {
        String text = content == null ? "" : content.strip();
        if (text.isBlank() || text.length() > 2000) throw new IllegalArgumentException("Коментарът трябва да е между 1 и 2000 символа.");
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Събитието не съществува."));
        if (parentId != null) commentRepository.findById(parentId).filter(c -> c.getEvent().getId().equals(eventId) && !c.isDeleted()).orElseThrow(() -> new IllegalArgumentException("Невалиден родителски коментар."));
        return commentRepository.save(new Comment(event, author, parentId, text));
    }

    @Transactional
    public void edit(Long id, String content, User user) {
        Comment comment = get(id);
        if (!comment.getAuthor().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) throw new SecurityException("Можете да редактирате само свои коментари.");
        comment.edit(content.strip());
    }

    @Transactional
    public void delete(Long id, User user) {
        Comment comment = get(id);
        if (!comment.getAuthor().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) throw new SecurityException("Нямате право да изтриете този коментар.");
        comment.moderateDelete();
    }
    @Transactional(readOnly = true)
    public Long eventId(Long id) { return get(id).getEvent().getId(); }
    private Comment get(Long id) { return commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Коментарът не съществува.")); }
}
