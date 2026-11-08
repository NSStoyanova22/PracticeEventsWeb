package com.example.hefest.service;
import com.example.hefest.model.*;
import com.example.hefest.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class CollectionService {
    private final FavoriteRepository favoriteRepository; private final UserCollectionRepository collectionRepository;
    private final CollectionItemRepository itemRepository; private final EventRepository eventRepository;
    public CollectionService(FavoriteRepository favoriteRepository, UserCollectionRepository collectionRepository,
                             CollectionItemRepository itemRepository, EventRepository eventRepository) {
        this.favoriteRepository=favoriteRepository; this.collectionRepository=collectionRepository; this.itemRepository=itemRepository; this.eventRepository=eventRepository;
    }
    @Transactional public void toggleFavorite(Long eventId, User user) {
        var existing = favoriteRepository.findByUserIdAndEventId(user.getId(), eventId);
        if (existing.isPresent()) favoriteRepository.delete(existing.get()); else favoriteRepository.save(new Favorite(user, event(eventId)));
    }
    @Transactional(readOnly=true) public List<Favorite> favorites(User user) { return favoriteRepository.findByUserId(user.getId()); }
    @Transactional(readOnly=true) public boolean isFavorite(Long eventId, User user) { return user != null && favoriteRepository.findByUserIdAndEventId(user.getId(), eventId).isPresent(); }
    @Transactional(readOnly=true) public List<UserCollection> collections(User user) { return collectionRepository.findByUserIdOrderByNameAsc(user.getId()); }
    @Transactional public void create(String name, User user) { String n=name==null?"":name.strip(); if(n.isBlank()) throw new IllegalArgumentException("Името на колекцията е задължително."); if(collectionRepository.existsByUserIdAndNameIgnoreCase(user.getId(),n)) throw new IllegalArgumentException("Тази колекция вече съществува."); collectionRepository.save(new UserCollection(user,n)); }
    @Transactional public void addToCollection(Long collectionId, Long eventId, User user) { var c=collectionRepository.findByIdAndUserId(collectionId,user.getId()).orElseThrow(() -> new SecurityException("Чужда колекция.")); if(!itemRepository.existsByCollectionIdAndEventId(collectionId,eventId)) itemRepository.save(new CollectionItem(c,event(eventId))); }
    @Transactional(readOnly=true) public List<CollectionItem> items(Long collectionId, User user) { collectionRepository.findByIdAndUserId(collectionId,user.getId()).orElseThrow(() -> new SecurityException("Чужда колекция.")); return itemRepository.findByCollectionId(collectionId); }
    private Event event(Long id) { return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Събитието не съществува.")); }
}
