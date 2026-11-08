package com.example.hefest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageService {

    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final Path uploadDir;

    public FileStorageService(@Value("${hefest.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    /**
     * Stores the file under a random name and returns that name.
     * Throws IllegalStateException when the content type does not start with the expected prefix (e.g. "image/").
     */
    public String store(MultipartFile file, String expectedTypePrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("Празен файл не може да бъде качен.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalStateException("Файлът е твърде голям. Максималният размер е 10 MB.");
        }
        String contentType = file.getContentType();
        if (!allowedContentType(contentType, expectedTypePrefix)) {
            throw new IllegalStateException("Файлът \"" + file.getOriginalFilename() + "\" не е от позволения тип.");
        }

        String fileName = UUID.randomUUID() + extensionOf(file.getOriginalFilename());
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(uploadDir);
            Files.copy(in, uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Неуспешно записване на файл.", e);
        }
        return fileName;
    }

    private static boolean allowedContentType(String contentType, String expectedTypePrefix) {
        if (contentType == null) return false;
        return switch (expectedTypePrefix) {
            case "image/" -> java.util.Set.of("image/jpeg", "image/png", "image/gif", "image/webp")
                    .contains(contentType.toLowerCase(Locale.ROOT));
            case "video/" -> java.util.Set.of("video/mp4", "video/webm", "video/quicktime")
                    .contains(contentType.toLowerCase(Locale.ROOT));
            default -> false;
        };
    }

    public void delete(String fileName) {
        Path target = uploadDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadDir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // A leftover file is harmless; the database row is what matters.
        }
    }

    private static String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String ext = originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ext.matches("[a-z0-9]{1,8}") ? "." + ext : "";
    }
}
