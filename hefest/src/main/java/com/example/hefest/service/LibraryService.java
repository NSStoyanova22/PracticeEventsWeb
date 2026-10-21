package com.example.hefest.service;

import com.example.hefest.model.LibraryResource;
import com.example.hefest.repository.LibraryResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class LibraryService {

    private final LibraryResourceRepository libraryResourceRepository;
    private final FileStorageService fileStorageService;

    public LibraryService(LibraryResourceRepository libraryResourceRepository,
                          FileStorageService fileStorageService) {
        this.libraryResourceRepository = libraryResourceRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<LibraryResource> getAllResources() {
        return libraryResourceRepository.findAllByOrderByCreatedAtDesc();
    }

    public LibraryResource getResource(Long id) {
        return libraryResourceRepository.findById(id)
                .orElseThrow();
    }

    @Transactional
    public LibraryResource createResource(LibraryResource form,
                                          List<MultipartFile> imageFiles,
                                          List<MultipartFile> videoFiles) {
        LibraryResource resource = new LibraryResource();
        copyText(form, resource);
        storeAll(imageFiles, "image/", resource.getImages());
        storeAll(videoFiles, "video/", resource.getVideos());
        return libraryResourceRepository.save(resource);
    }

    @Transactional
    public LibraryResource updateResource(Long id,
                                          LibraryResource form,
                                          List<MultipartFile> imageFiles,
                                          List<MultipartFile> videoFiles,
                                          List<String> removeImages,
                                          List<String> removeVideos) {
        LibraryResource resource = getResource(id);
        copyText(form, resource);
        // Store first: if an upload is rejected, nothing has been deleted yet.
        storeAll(imageFiles, "image/", resource.getImages());
        storeAll(videoFiles, "video/", resource.getVideos());
        removeAll(removeImages, resource.getImages());
        removeAll(removeVideos, resource.getVideos());
        return libraryResourceRepository.save(resource);
    }

    @Transactional
    public void deleteResource(Long id) {
        LibraryResource resource = getResource(id);
        List<String> files = new ArrayList<>(resource.getImages());
        files.addAll(resource.getVideos());
        libraryResourceRepository.delete(resource);
        files.forEach(fileStorageService::delete);
    }

    private static void copyText(LibraryResource from, LibraryResource to) {
        to.setTitle(from.getTitle().trim());
        to.setDescription(from.getDescription().trim());
        to.setSources(from.getSources() == null ? null : from.getSources().trim());
    }

    private void storeAll(List<MultipartFile> files, String typePrefix, List<String> target) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                target.add(fileStorageService.store(file, typePrefix));
            }
        }
    }

    private void removeAll(List<String> toRemove, List<String> target) {
        if (toRemove == null) {
            return;
        }
        for (String fileName : toRemove) {
            if (target.remove(fileName)) {
                fileStorageService.delete(fileName);
            }
        }
    }
}
