package com.example.hefest.controller;

import com.example.hefest.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public String library(Model model) {
        model.addAttribute("resources", libraryService.getAllResources());
        return "library";
    }

    @GetMapping("/{id}")
    public String resourceDetails(@PathVariable Long id, Model model) {
        model.addAttribute("libraryResource", libraryService.getResource(id));
        return "library-detail";
    }
}
