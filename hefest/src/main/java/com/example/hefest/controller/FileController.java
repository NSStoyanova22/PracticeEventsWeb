package com.example.hefest.controller;

import com.example.hefest.repository.LibraryResourceRepository;
import com.example.hefest.service.FileStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class FileController {
    private final FileStorageService fileStorageService;
    private final LibraryResourceRepository libraryResourceRepository;

    public FileController(FileStorageService fileStorageService,
                          LibraryResourceRepository libraryResourceRepository) {
        this.fileStorageService = fileStorageService;
        this.libraryResourceRepository = libraryResourceRepository;
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String filename) throws IOException {
        boolean attached = libraryResourceRepository.findAll().stream()
                .anyMatch(resource -> resource.getImages().contains(filename) || resource.getVideos().contains(filename));
        Path file = fileStorageService.getUploadDir().resolve(filename).normalize();
        if (!attached || !file.startsWith(fileStorageService.getUploadDir()) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        String contentType = Files.probeContentType(file);
        MediaType mediaType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok().contentType(mediaType).body(new InputStreamResource(Files.newInputStream(file)));
    }
}
