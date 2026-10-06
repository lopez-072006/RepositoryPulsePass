package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.enums.TicketType;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/** Academic example pricing in COP; prices are owned by the service layer, never the request. */
@Component
public class TicketPricingPolicy {
    public BigDecimal priceFor(TicketType type) {
        if (type == null) throw new BusinessRuleException("Ticket type is required.");
        return switch (type) {
            case GENERAL -> new BigDecimal("100000.00");
            case STUDENT -> new BigDecimal("70000.00");
            case VIP -> new BigDecimal("150000.00");
            case BACKSTAGE -> new BigDecimal("250000.00");
        };
    }
}
