package com.example.hefest.repository;

import com.example.hefest.model.LibraryResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryResourceRepository
        extends JpaRepository<LibraryResource, Long> {

    List<LibraryResource> findAllByOrderByCreatedAtDesc();
}
