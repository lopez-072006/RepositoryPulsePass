package com.Unimag.PulsePass.service;

import com.Unimag.PulsePass.dto.response.ArtistResponse;
import java.util.List;

public interface ArtistService {
    ArtistResponse findById(Long id);
    ArtistResponse findByStageName(String stageName);
    List<ArtistResponse> findActiveArtists();
}
