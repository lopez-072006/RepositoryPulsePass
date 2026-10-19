package com.Unimag.PulsePass.security;

import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.repository.EventRepository;
import com.Unimag.PulsePass.repository.TicketRepository;
import com.Unimag.PulsePass.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
    @Mock UserRepository users;
    @Mock TicketRepository tickets;
    @Mock EventRepository events;
    @InjectMocks AuthorizationService authorization;

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void purchaseAndUserLookupRequireSamePersistentUser() {
        User owner = user(7L, "andrea", "andrea@example.com");
        when(users.findByEmailIgnoreCase(owner.getEmail())).thenReturn(Optional.of(owner));
        authenticate(owner, "USER");

        assertThat(authorization.canPurchase(new com.Unimag.PulsePass.dto.request.PurchaseTicketRequest(
                owner.getEmail(), "EVT-1", com.Unimag.PulsePass.domain.enums.TicketType.GENERAL))).isTrue();
        assertThat(authorization.canReadUserByEmail(owner.getEmail())).isTrue();
        assertThat(authorization.canPurchase(new com.Unimag.PulsePass.dto.request.PurchaseTicketRequest(
                "other@example.com", "EVT-1", com.Unimag.PulsePass.domain.enums.TicketType.GENERAL))).isFalse();
    }

    @Test
    void ticketOwnershipAndStaffCapabilitiesAreDistinct() {
        User owner = user(7L, "andrea", "andrea@example.com");
        when(tickets.existsByTicketCodeAndUser_Id("TKT-1", 7L)).thenReturn(true);
        authenticate(owner, "USER");
        assertThat(authorization.canAccessTicket("TKT-1")).isTrue();
        assertThat(authorization.canCancelTicket("TKT-1")).isTrue();

        authenticate(user(8L, "carlos", "carlos@example.com"), "USER");
        assertThat(authorization.canAccessTicket("TKT-1")).isFalse();
        assertThat(authorization.canCancelTicket("TKT-1")).isFalse();

        authenticate(user(9L, "laura", "laura@example.com"), "STAFF");
        assertThat(authorization.canAccessTicket("TKT-1")).isTrue();
        assertThat(authorization.canCancelTicket("TKT-1")).isFalse();
    }

    @Test
    void publicEventLookupHidesDraftsFromNonPrivilegedUsers() {
        Event published = new Event();
        published.setStatus(EventStatus.PUBLISHED);
        Event draft = new Event();
        draft.setStatus(EventStatus.DRAFT);
        when(events.findByEventCode("PUB")).thenReturn(Optional.of(published));
        when(events.findByEventCode("DRAFT")).thenReturn(Optional.of(draft));

        SecurityContextHolder.clearContext();
        assertThat(authorization.canReadEvent("PUB")).isTrue();
        assertThat(authorization.canReadEvent("DRAFT")).isFalse();
        authenticate(user(9L, "laura", "laura@example.com"), "STAFF");
        assertThat(authorization.canReadEvent("DRAFT")).isTrue();
    }

    private void authenticate(User user, String role) {
        SecurityPrincipal principal = new SecurityPrincipal(user, "unused", List.of(role));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
    }

    private User user(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setActive(true);
        return user;
    }
}
