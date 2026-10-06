package com.Unimag.PulsePass.dto.response;

import com.Unimag.PulsePass.domain.enums.TicketStatus;
import com.Unimag.PulsePass.domain.enums.TicketType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(Long id, String ticketCode, TicketType type, BigDecimal price,
                             TicketStatus status, LocalDateTime purchaseDate,
                             String userEmail, String eventCode, String eventName) { }
