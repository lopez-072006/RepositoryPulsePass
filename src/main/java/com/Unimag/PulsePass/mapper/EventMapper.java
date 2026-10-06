package com.Unimag.PulsePass.mapper;

import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.dto.response.EventResponse;
import com.Unimag.PulsePass.dto.response.EventSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ArtistMapper.class)
public interface EventMapper {
    @Mapping(target = "venueCode", source = "venue.code")
    @Mapping(target = "venueName", source = "venue.name")
    EventResponse toResponse(Event event);

    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "city", source = "venue.city")
    EventSummaryResponse toSummary(Event event);
}
