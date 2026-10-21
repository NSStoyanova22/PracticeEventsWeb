package com.example.hefest.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class LibraryResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Заглавието е задължително.")
    @Size(max = 255, message = "Заглавието може да е до 255 символа.")
    private String title;

    @NotBlank(message = "Описанието е задължително.")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String sources;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "library_resource_images", joinColumns = @JoinColumn(name = "resource_id"))
    @OrderColumn(name = "position")
    @Column(name = "file_name")
    private List<String> images = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "library_resource_videos", joinColumns = @JoinColumn(name = "resource_id"))
    @OrderColumn(name = "position")
    @Column(name = "file_name")
    private List<String> videos = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public LibraryResource() {
    }

    /** Sources split into one entry per non-blank line. */
    public List<String> getSourceList() {
        if (sources == null) {
            return List.of();
        }
        return sources.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSources() { return sources; }
    public void setSources(String sources) { this.sources = sources; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<String> getVideos() { return videos; }
    public void setVideos(List<String> videos) { this.videos = videos; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
