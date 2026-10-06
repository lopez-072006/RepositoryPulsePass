package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.enums.TicketType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class TicketPricingPolicyTest {
    private final TicketPricingPolicy pricingPolicy = new TicketPricingPolicy();

    @Test
    void priceFor_returnsConfiguredPriceForEachTicketType() {
        assertThat(pricingPolicy.priceFor(TicketType.GENERAL)).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(pricingPolicy.priceFor(TicketType.STUDENT)).isEqualByComparingTo(new BigDecimal("70000.00"));
        assertThat(pricingPolicy.priceFor(TicketType.VIP)).isEqualByComparingTo(new BigDecimal("150000.00"));
        assertThat(pricingPolicy.priceFor(TicketType.BACKSTAGE)).isEqualByComparingTo(new BigDecimal("250000.00"));
    }
}
