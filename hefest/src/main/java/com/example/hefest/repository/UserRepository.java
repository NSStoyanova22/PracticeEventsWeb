package com.example.hefest.repository;

import com.example.hefest.model.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    List<User> findAllByOrderByNameAsc();
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            String name, String email, String phone, Pageable pageable);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
