package com.Unimag.PulsePass.service;

import com.Unimag.PulsePass.dto.response.VenueResponse;
import java.util.List;

public interface VenueService {
    VenueResponse findByCode(String code);
    List<VenueResponse> findActiveVenues();
}
