package com.Unimag.PulsePass.mapper;

import com.Unimag.PulsePass.domain.Venue;
import com.Unimag.PulsePass.dto.response.VenueResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VenueMapper {
    VenueResponse toResponse(Venue venue);
}
