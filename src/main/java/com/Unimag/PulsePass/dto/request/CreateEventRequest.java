package com.Unimag.PulsePass.dto.request;

import com.Unimag.PulsePass.domain.enums.EventCategory;
import java.time.LocalDateTime;

public record CreateEventRequest(
        String eventCode,
        String name,
        String description,
        EventCategory category,
        LocalDateTime eventDate,
        Integer minimumAge,
        String venueCode
) { }
