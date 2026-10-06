package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.domain.Ticket;
import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.domain.enums.TicketStatus;
import com.Unimag.PulsePass.dto.request.PurchaseTicketRequest;
import com.Unimag.PulsePass.dto.response.TicketResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.mapper.TicketMapper;
import com.Unimag.PulsePass.repository.EventRepository;
import com.Unimag.PulsePass.repository.TicketRepository;
import com.Unimag.PulsePass.repository.UserRepository;
import com.Unimag.PulsePass.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;
    private final TicketPricingPolicy pricingPolicy;

    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository userRepository,
                             EventRepository eventRepository, TicketMapper ticketMapper,
                             TicketPricingPolicy pricingPolicy) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
        this.pricingPolicy = pricingPolicy;
    }

    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        if (request == null) throw new BusinessRuleException("Purchase request is required.");
        if (request.userEmail() == null || request.userEmail().isBlank())
            throw new BusinessRuleException("User email is required.");
        if (request.eventCode() == null || request.eventCode().isBlank())
            throw new BusinessRuleException("Event code is required.");
        if (request.type() == null) throw new BusinessRuleException("Ticket type is required.");
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userEmail()));
        if (!Boolean.TRUE.equals(user.getActive()))
            throw new BusinessRuleException("Inactive users cannot purchase tickets.");

        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + request.eventCode()));
        if (event.getStatus() != EventStatus.PUBLISHED)
            throw new BusinessRuleException("Tickets can only be purchased for PUBLISHED events.");
        if (!event.getEventDate().isAfter(LocalDateTime.now()))
            throw new BusinessRuleException("Tickets cannot be purchased for an event that has already occurred.");
        validateAge(user, event);

        long paidTickets = ticketRepository.countPaidTicketsByEventCode(event.getEventCode(), TicketStatus.PAID);
        int capacity = event.getVenue().getCapacity();
        if (paidTickets >= capacity)
            throw new BusinessRuleException("Event has reached venue capacity: " + event.getEventCode());

        Ticket ticket = new Ticket();
        ticket.setTicketCode("TKT-" + UUID.randomUUID().toString().toUpperCase());
        ticket.setType(request.type());
        ticket.setPrice(pricingPolicy.priceFor(request.type()));
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        Ticket savedTicket = ticketRepository.save(ticket);

        if (paidTickets + 1 == capacity) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }
        return ticketMapper.toResponse(savedTicket);
    }

    @Override
    public TicketResponse findByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode).map(ticketMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
    }

    @Override
    public List<TicketResponse> findByUserEmail(String email) {
        return ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(email).stream()
                .map(ticketMapper::toResponse).toList();
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        return ticketRepository.findByEventEventCodeAndStatus(eventCode, TicketStatus.PAID).stream()
                .map(ticketMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = getTicket(ticketCode);
        if (ticket.getStatus() != TicketStatus.PAID)
            throw new BusinessRuleException("Only PAID tickets can be cancelled.");
        if (!ticket.getEvent().getEventDate().isAfter(LocalDateTime.now()))
            throw new BusinessRuleException("Tickets cannot be cancelled after the event has started.");
        ticket.setStatus(TicketStatus.CANCELLED);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = getTicket(ticketCode);
        if (ticket.getStatus() != TicketStatus.PAID)
            throw new BusinessRuleException("Only PAID tickets can be marked as used.");
        ticket.setStatus(TicketStatus.USED);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    private void validateAge(User user, Event event) {
        if (event.getMinimumAge() == 0) return;
        if (user.getUserProfile() == null || user.getUserProfile().getBirthDate() == null)
            throw new BusinessRuleException("User birth date is required to validate event age restrictions.");
        int ageAtEvent = Period.between(user.getUserProfile().getBirthDate(),
                event.getEventDate().toLocalDate()).getYears();
        if (ageAtEvent < event.getMinimumAge())
            throw new BusinessRuleException("User does not meet minimum age for event: " + event.getEventCode());
    }

    private Ticket getTicket(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));
    }
}
