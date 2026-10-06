package com.Unimag.PulsePass.dto.request;

import com.Unimag.PulsePass.domain.enums.TicketType;

public record PurchaseTicketRequest(String userEmail, String eventCode, TicketType type) { }
