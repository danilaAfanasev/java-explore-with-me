package ru.practicum.event;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    List<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findByIdIn(List<Long> eventIds);

    Event findFirstByCategoryId(Long catId);

    @Query(value = "SELECT * FROM Events e WHERE " +
            "(:userIds IS NULL OR (SELECT COUNT(*) FROM UNNEST(:userIds)) = 0 OR e.initiator_id IN (:userIds)) " +
            "AND (:states IS NULL OR (SELECT COUNT(*) FROM UNNEST(:states)) = 0 OR e.state IN (:states)) " +
            "AND (:categories IS NULL OR (SELECT COUNT(*) FROM UNNEST(:categories)) = 0 OR e.category_id IN (:categories)) " +
            "AND (:rangeStart IS NULL OR e.event_date >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.event_date < :rangeEnd) " +
            "ORDER BY e.event_date DESC",
            nativeQuery = true)
    List<Event> findEvents(@Param("userIds") List<Long> userIds,
                           @Param("states") List<String> states,
                           @Param("categories") List<Long> categories,
                           @Param("rangeStart") LocalDateTime rangeStart,
                           @Param("rangeEnd") LocalDateTime rangeEnd,
                           Pageable pageable);

    @Query(value = "SELECT * FROM Events e WHERE (e.state = 'PUBLISHED') "
            + "and (:text is null or lower(e.annotation) LIKE lower(concat('%',cast(:text AS text),'%')) "
            + "or lower(e.description) LIKE lower(concat('%',cast(:text AS text),'%'))) "
            + "and (:categories is null or e.category_id IN (cast(cast(:categories AS TEXT) AS BIGINT))) "
            + "and (:paid is null or e.paid = cast(cast(:paid AS text) AS BOOLEAN)) "
            + "and (e.event_date >= :rangeStart) "
            + "and (cast(:rangeEnd AS timestamp) is null or e.event_date < cast(:rangeEnd AS timestamp))",
            nativeQuery = true)

    List<Event> findPublishedEvents(String text, List<Long> categories, Boolean paid,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("rangeEnd") LocalDateTime rangeEnd, Pageable pageable);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id = :eventId")
    void incrementViews(@Param("eventId") Long eventId);

}