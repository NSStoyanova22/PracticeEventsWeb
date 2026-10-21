package com.example.hefest.controller;

import com.example.hefest.model.LibraryResource;
import com.example.hefest.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/library")
public class LibraryAdminController {

    private final LibraryService libraryService;

    public LibraryAdminController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @InitBinder("libraryResource")
    void allowOnlyTextFields(WebDataBinder binder) {
        binder.setAllowedFields("title", "description", "sources");
    }

    @GetMapping("/new")
    public String newResourceForm(Model model) {
        model.addAttribute("libraryResource", new LibraryResource());
        model.addAttribute("isEdit", false);
        return "admin/library-form";
    }

    @PostMapping
    public String createResource(@Valid @ModelAttribute("libraryResource") LibraryResource form,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) List<MultipartFile> imageFiles,
                                 @RequestParam(required = false) List<MultipartFile> videoFiles,
                                 Model model) {
        model.addAttribute("isEdit", false);
        if (bindingResult.hasErrors()) {
            return "admin/library-form";
        }

        try {
            LibraryResource saved = libraryService.createResource(form, imageFiles, videoFiles);
            return "redirect:/library/" + saved.getId();
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/library-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editResourceForm(@PathVariable Long id, Model model) {
        model.addAttribute("libraryResource", libraryService.getResource(id));
        model.addAttribute("isEdit", true);
        return "admin/library-form";
    }

    @PostMapping("/{id}")
    public String updateResource(@PathVariable Long id,
                                 @Valid @ModelAttribute("libraryResource") LibraryResource form,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) List<MultipartFile> imageFiles,
                                 @RequestParam(required = false) List<MultipartFile> videoFiles,
                                 @RequestParam(required = false) List<String> removeImages,
                                 @RequestParam(required = false) List<String> removeVideos,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return editFormWithErrors(id, form, model, null);
        }

        try {
            libraryService.updateResource(id, form, imageFiles, videoFiles, removeImages, removeVideos);
            return "redirect:/library/" + id;
        } catch (IllegalStateException e) {
            return editFormWithErrors(id, form, model, e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteResource(@PathVariable Long id) {
        libraryService.deleteResource(id);
        return "redirect:/library";
    }

    private String editFormWithErrors(Long id, LibraryResource form, Model model, String errorMessage) {
        // Keep the typed text but show the media that is actually stored.
        LibraryResource stored = libraryService.getResource(id);
        form.setId(id);
        form.setImages(stored.getImages());
        form.setVideos(stored.getVideos());
        model.addAttribute("isEdit", true);
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "admin/library-form";
    }
}
