package com.example.hefest.service;

import com.example.hefest.model.ChatConversation;
import com.example.hefest.model.ChatMessage;
import com.example.hefest.model.User;
import com.example.hefest.repository.ChatConversationRepository;
import com.example.hefest.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChatService {

    public static final int MAX_MESSAGE_LENGTH = 2000;
    private static final int PREVIEW_LENGTH = 120;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM HH:mm");

    public record MessageView(Long id, String content, boolean fromAdmin, String senderName, String time) {
    }

    public record ConversationView(Long id, String userName, String userEmail, String preview,
                                   String time, boolean waiting) {
    }

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(ChatConversationRepository conversationRepository,
                       ChatMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public List<MessageView> getUserMessages(User user, long afterId) {
        return conversationRepository.findByUserId(user.getId())
                .map(conversation -> messagesAfter(conversation.getId(), afterId))
                .orElse(List.of());
    }

    @Transactional
    public MessageView postUserMessage(User user, String content) {
        ChatConversation conversation = conversationRepository.findByUserId(user.getId())
                .orElseGet(() -> conversationRepository.save(new ChatConversation(user)));
        return post(conversation, user, false, content);
    }

    @Transactional(readOnly = true)
    public List<ConversationView> listConversations() {
        return conversationRepository.findAllByOrderByWaitingForAdminDescLastMessageAtDesc().stream()
                .map(c -> new ConversationView(
                        c.getId(),
                        c.getUser().getName(),
                        c.getUser().getEmail(),
                        c.getLastMessagePreview(),
                        format(c.getLastMessageAt()),
                        c.isWaitingForAdmin()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageView> getConversationMessages(Long conversationId, long afterId) {
        return messagesAfter(conversationId, afterId);
    }

    @Transactional
    public MessageView postAdminMessage(User admin, Long conversationId, String content) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalStateException("Разговорът не съществува."));
        return post(conversation, admin, true, content);
    }

    public long waitingCount() {
        return conversationRepository.countByWaitingForAdminTrue();
    }

    private MessageView post(ChatConversation conversation, User sender, boolean fromAdmin, String content) {
        String text = content == null ? "" : content.strip();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Съобщението е празно.");
        }
        if (text.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Съобщението може да е до " + MAX_MESSAGE_LENGTH + " символа.");
        }

        ChatMessage message = messageRepository.save(new ChatMessage(conversation, sender, fromAdmin, text));
        conversation.setWaitingForAdmin(!fromAdmin);
        conversation.setLastMessageAt(message.getCreatedAt());
        conversation.setLastMessagePreview(text.length() > PREVIEW_LENGTH
                ? text.substring(0, PREVIEW_LENGTH) + "…"
                : text);
        conversationRepository.save(conversation);
        return toView(message);
    }

    private List<MessageView> messagesAfter(Long conversationId, long afterId) {
        return messageRepository.findByConversationIdAndIdGreaterThanOrderByIdAsc(conversationId, afterId)
                .stream()
                .map(ChatService::toView)
                .toList();
    }

    private static MessageView toView(ChatMessage message) {
        return new MessageView(
                message.getId(),
                message.getContent(),
                message.isFromAdmin(),
                message.getSender().getName(),
                format(message.getCreatedAt()));
    }

    private static String format(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.toLocalDate().equals(LocalDate.now()) ? TIME.format(time) : DATE_TIME.format(time);
    }
}
