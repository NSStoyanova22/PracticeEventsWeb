package com.example.hefest.repository;

import com.example.hefest.model.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository
        extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByUserId(Long userId);
    List<ChatConversation> findAllByOrderByWaitingForAdminDescLastMessageAtDesc();
    long countByWaitingForAdminTrue();
}
