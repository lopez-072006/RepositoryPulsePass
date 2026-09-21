package com.Unimag.PulsePass.repository;

import com.Unimag.PulsePass.domain.Ticket;
import com.Unimag.PulsePass.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserEmailAndStatus(
            String email,
            TicketStatus status
    );

}