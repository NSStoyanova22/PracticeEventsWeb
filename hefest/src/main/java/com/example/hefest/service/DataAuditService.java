package com.example.hefest.service;

import com.example.hefest.model.ChatMessage;
import com.example.hefest.model.Event;
import com.example.hefest.model.InvalidRecord;
import com.example.hefest.model.LibraryResource;
import com.example.hefest.model.Registration;
import com.example.hefest.model.User;
import com.example.hefest.model.UserSession;
import com.example.hefest.repository.ChatMessageRepository;
import com.example.hefest.repository.EventRepository;
import com.example.hefest.repository.LibraryResourceRepository;
import com.example.hefest.repository.RegistrationRepository;
import com.example.hefest.repository.UserRepository;
import com.example.hefest.repository.UserSessionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Scans every table and reports rows that break the application's rules.
 * The checks mirror the validation used by the forms, plus rules that only make sense across rows
 * (duplicate e-mails, over-booked events, missing media files, ...). Rows like these can appear
 * when data is edited directly in the database or was saved before a rule existed.
 */
@Service
public class DataAuditService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public record TableCheck(String table, String title, int checked, List<InvalidRecord> invalid) {
    }

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final LibraryResourceRepository libraryResourceRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserSessionRepository userSessionRepository;
    private final FileStorageService fileStorageService;
    private final Validator validator;

    public DataAuditService(UserRepository userRepository,
                            EventRepository eventRepository,
                            RegistrationRepository registrationRepository,
                            LibraryResourceRepository libraryResourceRepository,
                            ChatMessageRepository chatMessageRepository,
                            UserSessionRepository userSessionRepository,
                            FileStorageService fileStorageService,
                            Validator validator) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.libraryResourceRepository = libraryResourceRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userSessionRepository = userSessionRepository;
        this.fileStorageService = fileStorageService;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<TableCheck> checkAllTables() {
        return List.of(
                checkUsers(),
                checkEvents(),
                checkRegistrations(),
                checkLibraryResources(),
                checkChatMessages(),
                checkSessions());
    }

    private TableCheck checkUsers() {
        List<User> users = userRepository.findAll();

        Map<String, Long> emailCounts = new HashMap<>();
        for (User user : users) {
            if (!isBlank(user.getEmail())) {
                emailCounts.merge(user.getEmail().trim().toLowerCase(Locale.ROOT), 1L, Long::sum);
            }
        }

        return check("users", "Потребители", users, User::getId, User::getEmail, user -> {
            List<String> problems = new ArrayList<>();
            if (isBlank(user.getName())) problems.add("Липсва име.");
            if (isBlank(user.getEmail())) {
                problems.add("Липсва имейл.");
            } else {
                if (!EMAIL.matcher(user.getEmail().trim()).matches()) problems.add("Невалиден имейл.");
                if (emailCounts.get(user.getEmail().trim().toLowerCase(Locale.ROOT)) > 1) {
                    problems.add("Имейлът се повтаря при друг потребител.");
                }
            }
            if (isBlank(user.getPhone())) problems.add("Липсва телефон.");
            if (user.getRole() == null) problems.add("Липсва роля.");
            if (isBlank(user.getPassword())) problems.add("Липсва парола.");
            return problems;
        });
    }

    private TableCheck checkEvents() {
        List<Event> events = eventRepository.findAll();

        Map<Long, Long> registrations = new HashMap<>();
        for (Object[] row : registrationRepository.countPerEvent()) {
            registrations.put((Long) row[0], (Long) row[1]);
        }

        return check("event", "Събития", events, Event::getId, Event::getTitle, event -> {
            List<String> problems = violations(event);
            long registered = registrations.getOrDefault(event.getId(), 0L);
            if (event.getCapacity() > 0 && registered > event.getCapacity()) {
                problems.add("Препълнено: " + registered + " регистрации при капацитет " + event.getCapacity() + ".");
            }
            return problems;
        });
    }

    private TableCheck checkRegistrations() {
        List<Registration> registrations = registrationRepository.findAll();
        return check("registration", "Регистрации", registrations, Registration::getId,
                r -> (r.getUser() == null ? "?" : r.getUser().getEmail())
                        + " → " + (r.getEvent() == null ? "?" : r.getEvent().getTitle()),
                r -> {
                    List<String> problems = new ArrayList<>();
                    if (r.getUser() == null) problems.add("Липсва потребител.");
                    else if (!r.getUser().isActive()) problems.add("Потребителят е деактивиран.");
                    if (r.getEvent() == null) problems.add("Липсва събитие.");
                    return problems;
                });
    }

    private TableCheck checkLibraryResources() {
        List<LibraryResource> resources = libraryResourceRepository.findAll();
        return check("library_resource", "Библиотека", resources, LibraryResource::getId, LibraryResource::getTitle,
                resource -> {
                    List<String> problems = violations(resource);
                    for (String file : resource.getImages()) {
                        if (missingFile(file)) problems.add("Липсва файл на снимка: " + file);
                    }
                    for (String file : resource.getVideos()) {
                        if (missingFile(file)) problems.add("Липсва файл на видео: " + file);
                    }
                    return problems;
                });
    }

    private TableCheck checkChatMessages() {
        List<ChatMessage> messages = chatMessageRepository.findAll();
        return check("chat_message", "Чат съобщения", messages, ChatMessage::getId,
                m -> m.getSender() == null ? "?" : m.getSender().getEmail(),
                m -> {
                    List<String> problems = new ArrayList<>();
                    if (isBlank(m.getContent())) problems.add("Празно съобщение.");
                    else if (m.getContent().length() > ChatService.MAX_MESSAGE_LENGTH) {
                        problems.add("Съобщението е над " + ChatService.MAX_MESSAGE_LENGTH + " символа.");
                    }
                    if (m.getCreatedAt() == null) problems.add("Липсва час на изпращане.");
                    return problems;
                });
    }

    private TableCheck checkSessions() {
        List<UserSession> sessions = userSessionRepository.findAll();
        return check("user_session", "Сесии", sessions, UserSession::getId,
                s -> s.getUser() == null ? "?" : s.getUser().getEmail(),
                s -> {
                    List<String> problems = new ArrayList<>();
                    if (s.getLoginAt() == null) problems.add("Липсва час на вход.");
                    if (s.getLoginAt() != null && s.getLogoutAt() != null && s.getLogoutAt().isBefore(s.getLoginAt())) {
                        problems.add("Изходът е преди входа.");
                    }
                    if (s.getLogoutAt() != null && s.getEndReason() == null) problems.add("Липсва причина за край.");
                    return problems;
                });
    }

    private <T> TableCheck check(String table, String title, List<T> rows,
                                 Function<T, Long> id, Function<T, String> label,
                                 Function<T, List<String>> rules) {
        List<InvalidRecord> invalid = new ArrayList<>();
        for (T row : rows) {
            List<String> problems = rules.apply(row);
            if (!problems.isEmpty()) {
                String name = label.apply(row);
                invalid.add(new InvalidRecord(table, id.apply(row), isBlank(name) ? "(без име)" : name, problems));
            }
        }
        return new TableCheck(table, title, rows.size(), invalid);
    }

    /** Bean Validation messages (@NotBlank, @Min, ...) declared on the entity. */
    private <T> List<String> violations(T entity) {
        List<String> problems = new ArrayList<>();
        for (ConstraintViolation<T> violation : validator.validate(entity)) {
            problems.add(violation.getMessage());
        }
        return problems;
    }

    private boolean missingFile(String fileName) {
        return isBlank(fileName) || !Files.exists(fileStorageService.getUploadDir().resolve(fileName));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
