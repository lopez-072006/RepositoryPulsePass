package com.Unimag.PulsePass.mapper;

import com.Unimag.PulsePass.domain.Artist;
import com.Unimag.PulsePass.dto.response.ArtistResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArtistMapper {
    ArtistResponse toResponse(Artist artist);
}
