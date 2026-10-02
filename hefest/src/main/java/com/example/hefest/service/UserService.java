package com.example.hefest.service;

import com.example.hefest.model.RegistrationForm;
import com.example.hefest.model.User;
import com.example.hefest.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.hefest.model.Role;
import com.example.hefest.model.UserForm;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email is already registered");
        }

        User user = new User();
        user.setName(form.getName().trim());
        user.setEmail(email);
        user.setPhone(form.getPhone().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        return userRepository.save(user);
    }
    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByNameAsc();
    }
    public User createUser(UserForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Email is already registered");
        }
        if(form.getRole() == null || form.getRole() == Role.USER) {
            throw new IllegalStateException("The selected role is not allowed");
        }
        User user = new User();
        user.setName(form.getName().trim());
        user.setEmail(email);
        user.setPhone(form.getPhone().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole());
        user.setActive(true);
        return userRepository.save(user);
    }

    public User updateUser(Long id, UserForm form) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new IllegalStateException("Email is already registered");
        }
        user.setName(form.getName().trim());
        user.setEmail(email);
        user.setPhone(form.getPhone().trim());
        
        return userRepository.save(user);
    }

    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }
    public void changeRole(Long id, Role role) {
       if(role == null || role == Role.USER) {
            throw new IllegalStateException("The selected role is not allowed");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }
    public User findById(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() ->
                    new IllegalStateException("User not found"));
}
}
