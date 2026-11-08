package com.example.hefest.repository;


import com.example.hefest.model.Event;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import com.example.hefest.model.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepository
        extends JpaRepository<Event, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select event from Event event where event.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
    List<Event> findByDate(LocalDate date);
    List<Event> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Event> findByDeletedAtIsNullAndDateAndStatus(LocalDate date, EventStatus status);
    List<Event> findByDeletedAtIsNullAndDateBeforeAndStatus(LocalDate date, EventStatus status);
    Optional<Event> findByIdAndDeletedAtIsNull(Long id);
    List<Event> findByDeletedAtIsNotNullOrderByDeletedAtDesc();
    List<Event> findByDeletedAtIsNullAndDateBetween(LocalDate startDate, LocalDate endDate);
    List<Event> findByDeletedAtIsNull();

    @Query("select e from Event e where " +
            "e.deletedAt is null and (:title = '' or lower(e.title) like lower(concat('%', :title, '%'))) and " +
            "(:fromDate is null or e.date >= :fromDate) and " +
            "(:toDate is null or e.date <= :toDate) and " +
            "(:minPrice is null or e.price >= :minPrice) and " +
            "(:maxPrice is null or e.price <= :maxPrice) and " +
            "(:minCapacity is null or e.capacity >= :minCapacity)")
    Page<Event> search(@Param("title") String title,
                       @Param("fromDate") LocalDate fromDate,
                       @Param("toDate") LocalDate toDate,
                       @Param("minPrice") java.math.BigDecimal minPrice,
                       @Param("maxPrice") java.math.BigDecimal maxPrice,
                       @Param("minCapacity") Integer minCapacity,
                       Pageable pageable);
}
