package com.Unimag.PulsePass.repository;

import com.Unimag.PulsePass.domain.Ticket;
import com.Unimag.PulsePass.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByUserEmailIgnoreCaseAndStatus(String email, TicketStatus status);

    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    @Query("""
        SELECT COUNT(t) FROM Ticket t
        WHERE t.event.eventCode = :eventCode
          AND t.status = :status
    """)
    long countPaidTicketsByEventCode(
            @Param("eventCode") String eventCode,
            @Param("status") TicketStatus status
    );
}