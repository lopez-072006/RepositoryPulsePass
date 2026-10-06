package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.domain.Ticket;
import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.domain.UserProfile;
import com.Unimag.PulsePass.domain.Venue;
import com.Unimag.PulsePass.domain.enums.EventCategory;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.domain.enums.TicketStatus;
import com.Unimag.PulsePass.domain.enums.TicketType;
import com.Unimag.PulsePass.dto.request.PurchaseTicketRequest;
import com.Unimag.PulsePass.dto.response.TicketResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.mapper.TicketMapper;
import com.Unimag.PulsePass.repository.EventRepository;
import com.Unimag.PulsePass.repository.TicketRepository;
import com.Unimag.PulsePass.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    @Mock TicketRepository ticketRepository;
    @Mock UserRepository userRepository;
    @Mock EventRepository eventRepository;
    @Mock TicketMapper ticketMapper;
    @Mock TicketPricingPolicy pricingPolicy;
    @InjectMocks TicketServiceImpl service;

    private User activeUser;
    private Event publishedEvent;
    private BigDecimal price;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setEmail("andrea@example.com");
        activeUser.setActive(true);
        UserProfile profile = new UserProfile();
        profile.setBirthDate(LocalDate.of(1990, 1, 1));
        profile.setUser(activeUser);
        activeUser.setUserProfile(profile);

        Venue venue = new Venue();
        venue.setCapacity(3);
        publishedEvent = new Event();
        publishedEvent.setId(2L);
        publishedEvent.setEventCode("EVT-1");
        publishedEvent.setName("Concert");
        publishedEvent.setCategory(EventCategory.MUSIC);
        publishedEvent.setStatus(EventStatus.PUBLISHED);
        publishedEvent.setEventDate(LocalDateTime.now().plusYears(1));
        publishedEvent.setMinimumAge(18);
        publishedEvent.setVenue(venue);
        price = new BigDecimal("150000.00");
    }

    @Test
    void purchase_validRequest_createsPaidTicket() {
        givenPurchasable(activeUser, publishedEvent, 0);
        when(pricingPolicy.priceFor(TicketType.VIP)).thenReturn(price);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));

        TicketResponse result = service.purchase(request());

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void purchase_whenUserDoesNotExist_throwsNotFound() {
        when(userRepository.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(ResourceNotFoundException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchase_whenUserIsInactive_throwsAndDoesNotSave() {
        activeUser.setActive(false);
        when(userRepository.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.of(activeUser));
        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchase_forDraftEvent_throwsAndDoesNotSave() {
        publishedEvent.setStatus(EventStatus.DRAFT);
        givenUserAndEvent(activeUser, publishedEvent);
        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchase_forCancelledEvent_throwsAndDoesNotSave() {
        publishedEvent.setStatus(EventStatus.CANCELLED);
        givenUserAndEvent(activeUser, publishedEvent);
        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchase_whenUserIsBelowMinimumAge_throwsAndDoesNotSave() {
        LocalDate eventDay = publishedEvent.getEventDate().toLocalDate();
        activeUser.getUserProfile().setBirthDate(eventDay.minusYears(17));
        givenPurchasable(activeUser, publishedEvent, 0);
        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchase_whenCapacityIsFull_throwsAndDoesNotSave() {
        givenPurchasable(activeUser, publishedEvent, 3);
        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void purchase_forLastAvailablePlace_savesTicketAndMarksEventSoldOut() {
        givenPurchasable(activeUser, publishedEvent, 2);
        when(pricingPolicy.priceFor(TicketType.VIP)).thenReturn(price);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventRepository.save(publishedEvent)).thenReturn(publishedEvent);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));

        service.purchase(request());

        assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        verify(ticketRepository).save(any(Ticket.class));
        verify(eventRepository).save(publishedEvent);
    }

    @Test
    void cancel_paidTicket_changesStatusToCancelled() {
        Ticket ticket = ticket(TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.CANCELLED));

        TicketResponse result = service.cancel("TKT-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void cancel_usedTicket_throwsAndDoesNotSave() {
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket(TicketStatus.USED)));
        assertThatThrownBy(() -> service.cancel("TKT-1")).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void markAsUsed_paidTicket_changesStatusToUsed() {
        Ticket ticket = ticket(TicketStatus.PAID);
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.USED));

        TicketResponse result = service.markAsUsed("TKT-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(result.status()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void markAsUsed_cancelledTicket_throwsAndDoesNotSave() {
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket(TicketStatus.CANCELLED)));
        assertThatThrownBy(() -> service.markAsUsed("TKT-1")).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    private void givenPurchasable(User user, Event event, long paidTickets) {
        givenUserAndEvent(user, event);
        when(ticketRepository.countPaidTicketsByEventCode("EVT-1", TicketStatus.PAID)).thenReturn(paidTickets);
    }

    private void givenUserAndEvent(User user, Event event) {
        when(userRepository.findByEmailIgnoreCase("andrea@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("EVT-1")).thenReturn(Optional.of(event));
    }

    private PurchaseTicketRequest request() {
        return new PurchaseTicketRequest("andrea@example.com", "EVT-1", TicketType.VIP);
    }

    private Ticket ticket(TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setTicketCode("TKT-1");
        ticket.setType(TicketType.VIP);
        ticket.setPrice(price);
        ticket.setStatus(status);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(activeUser);
        ticket.setEvent(publishedEvent);
        return ticket;
    }

    private TicketResponse response(TicketStatus status) {
        return new TicketResponse(1L, "TKT-1", TicketType.VIP, price, status,
                LocalDateTime.now(), "andrea@example.com", "EVT-1", "Concert");
    }
}
