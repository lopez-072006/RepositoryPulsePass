package com.Unimag.PulsePass.security;

import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.dto.request.PurchaseTicketRequest;
import com.Unimag.PulsePass.repository.EventRepository;
import com.Unimag.PulsePass.repository.TicketRepository;
import com.Unimag.PulsePass.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authorizationService")
public class AuthorizationService {
    private final UserRepository users;
    private final TicketRepository tickets;
    private final EventRepository events;

    public AuthorizationService(UserRepository users, TicketRepository tickets, EventRepository events) {
        this.users = users;
        this.tickets = tickets;
        this.events = events;
    }

    public boolean canReadEvent(String eventCode) {
        return events.findByEventCode(eventCode)
                .map(event -> event.getStatus() == EventStatus.PUBLISHED || hasAnyRole("STAFF", "ADMIN"))
                .orElse(true); // Preserve the service's normal 404 for an unknown code.
    }

    public boolean canReadUserByEmail(String email) {
        if (hasRole("ADMIN")) return true;
        SecurityPrincipal principal = principal();
        return principal != null && users.findByEmailIgnoreCase(email)
                .map(user -> user.getId().equals(principal.getUserId())).orElse(false);
    }

    public boolean canReadUserByUsername(String username) {
        if (hasRole("ADMIN")) return true;
        SecurityPrincipal principal = principal();
        return principal != null && users.findByUsernameIgnoreCase(username)
                .map(user -> user.getId().equals(principal.getUserId())).orElse(false);
    }

    public boolean canAccessTicket(String ticketCode) {
        if (hasAnyRole("STAFF", "ADMIN")) return true;
        SecurityPrincipal principal = principal();
        return principal != null && tickets.existsByTicketCodeAndUser_Id(ticketCode, principal.getUserId());
    }

    public boolean canCancelTicket(String ticketCode) {
        if (hasRole("ADMIN")) return true;
        SecurityPrincipal principal = principal();
        return principal != null && tickets.existsByTicketCodeAndUser_Id(ticketCode, principal.getUserId());
    }

    public boolean canReadTicketsByUserEmail(String email) {
        if (hasRole("ADMIN")) return true;
        SecurityPrincipal principal = principal();
        return principal != null && users.findByEmailIgnoreCase(email)
                .map(user -> user.getId().equals(principal.getUserId())).orElse(false);
    }

    public boolean canPurchase(PurchaseTicketRequest request) {
        if (request == null || request.userEmail() == null) return false;
        SecurityPrincipal principal = principal();
        return principal != null && users.findByEmailIgnoreCase(request.userEmail())
                .map(user -> user.getId().equals(principal.getUserId())).orElse(false);
    }

    public boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> java.util.Arrays.stream(roles)
                        .anyMatch(role -> authority.getAuthority().equals("ROLE_" + role)));
    }

    private boolean hasRole(String role) { return hasAnyRole(role); }

    private SecurityPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof SecurityPrincipal value ? value : null;
    }
}
