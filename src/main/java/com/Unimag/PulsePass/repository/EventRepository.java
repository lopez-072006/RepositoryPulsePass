package com.Unimag.PulsePass.repository;

import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventCode(String eventCode);

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findByVenueCode(String venueCode);

    @Query("""
        SELECT DISTINCT e FROM Event e
        JOIN e.artists a
        WHERE LOWER(a.stageName) = LOWER(:stageName)
    """)
    List<Event> findEventsByArtistStageName(@Param("stageName") String stageName);

    @Query("""
        SELECT DISTINCT e FROM Event e
        JOIN e.venue v
        JOIN e.artists a
        WHERE e.status = :status
          AND e.eventDate > :afterDate
          AND v.city = :city
          AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistName, '%'))
        ORDER BY e.eventDate ASC
    """)
    List<Event> findRecommendedEvents(
            @Param("status") EventStatus status,
            @Param("afterDate") LocalDateTime afterDate,
            @Param("city") String city,
            @Param("artistName") String artistName
    );
}