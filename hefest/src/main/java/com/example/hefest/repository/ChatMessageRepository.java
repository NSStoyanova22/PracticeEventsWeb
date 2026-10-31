package com.example.hefest.repository;

import com.example.hefest.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdAndIdGreaterThanOrderByIdAsc(Long conversationId, Long afterId);
}
