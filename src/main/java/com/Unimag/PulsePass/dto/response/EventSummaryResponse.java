package com.Unimag.PulsePass.dto.response;

import com.Unimag.PulsePass.domain.enums.EventCategory;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import java.time.LocalDateTime;

public record EventSummaryResponse(Long id, String eventCode, String name,
                                   EventCategory category, EventStatus status,
                                   LocalDateTime eventDate, String venueName, String city) { }
