package com.example.hefest.repository;
import com.example.hefest.model.UserCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserCollectionRepository extends JpaRepository<UserCollection, Long> {
    List<UserCollection> findByUserIdOrderByNameAsc(Long userId);
    Optional<UserCollection> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
