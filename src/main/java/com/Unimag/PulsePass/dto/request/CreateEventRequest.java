package com.Unimag.PulsePass.dto.request;

import com.Unimag.PulsePass.domain.enums.EventCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateEventRequest(

        @NotBlank(message = "Event code is required")
        String eventCode,

        @NotBlank(message = "Event name is required")
        String name,

        String description,

        @NotNull(message = "Event category is required")
        EventCategory category,

        @NotNull(message = "Event date is required")
        LocalDateTime eventDate,

        @NotNull(message = "Minimum age is required")
        @Min(value = 0, message = "Minimum age cannot be negative")
        Integer minimumAge,

        @NotBlank(message = "Venue code is required")
        String venueCode

) {
}
