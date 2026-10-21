package com.example.hefest.controller;

import com.example.hefest.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/managers")
public class ManagerController {

    private final UserRepository userRepository;

    public ManagerController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String managers(Model model) {
        model.addAttribute("users", userRepository.findAllByOrderByNameAsc());
        return "admin/managers";
    }
}
