
package com.example.hefest.controller;

import com.example.hefest.model.Role;
import com.example.hefest.model.User;
import com.example.hefest.model.UserForm;
import com.example.hefest.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller 
@RequestMapping ("/admin/users")
public class UserManagementController {
    private final UserService userService;
    public UserManagementController(UserService userService) {
        this.userService = userService;
    }
    private List<Role> availableRoles() {
        return List.of(Role.ADMIN, Role.USER);
    }
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<User> users = userService.searchUsers(search, sort, direction, page, size);
        String currentSort = switch (sort) {
            case "email", "phone", "role", "active" -> sort;
            default -> "name";
        };
        String currentDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
        model.addAttribute("users", users);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("sort", currentSort);
        model.addAttribute("direction", currentDirection);
        model.addAttribute("size", Math.min(Math.max(size, 1), 100));
        return "admin/users";
    }
    @GetMapping ("/new")
    public String newUserForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("roles", availableRoles());
        model.addAttribute("isEdit", false);
        model.addAttribute("actionUrl", "/admin/users");
        return "admin/user-form";
    }
    @PostMapping 
    public String createUser(@ModelAttribute ("userForm") UserForm form, Model model) {
        try {
            userService.createUser(form);
            return "redirect:/admin/users";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", availableRoles());
            model.addAttribute("isEdit", false);
            model.addAttribute("actionUrl", "/admin/users");
            return "admin/user-form";
        }
    }
     @GetMapping("/{id}/edit")
    public String editUserForm(
            @PathVariable Long id,
            Model model) {

        User user = userService.findById(id);

        UserForm form = new UserForm();
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole());
        form.setActive(user.isActive());

        model.addAttribute("userForm", form);
        model.addAttribute("roles", availableRoles());
        model.addAttribute("isEdit", true);
        model.addAttribute("actionUrl", "/admin/users/" + id);
        model.addAttribute("userId", id);

        return "admin/user-form";
    }

    @PostMapping("/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("userForm") UserForm form,
            Model model) {

        try {
            userService.updateUser(id, form);
            return "redirect:/admin/users";

        } catch (IllegalStateException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("roles", availableRoles());
            model.addAttribute("isEdit", true);
            model.addAttribute("actionUrl", "/admin/users/" + id);
            model.addAttribute("userId", id);

            return "admin/user-form";
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam Role role) {

        userService.changeRole(id, role);
        return "redirect:/admin/users";
    }

}
