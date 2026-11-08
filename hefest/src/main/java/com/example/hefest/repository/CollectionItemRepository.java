package com.example.hefest.repository;
import com.example.hefest.model.CollectionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CollectionItemRepository extends JpaRepository<CollectionItem, Long> {
    List<CollectionItem> findByCollectionId(Long collectionId);
    boolean existsByCollectionIdAndEventId(Long collectionId, Long eventId);
}
