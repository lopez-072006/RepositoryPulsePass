package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.dto.response.VenueResponse;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.mapper.VenueMapper;
import com.Unimag.PulsePass.repository.VenueRepository;
import com.Unimag.PulsePass.service.VenueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VenueServiceImpl implements VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    public VenueServiceImpl(VenueRepository venueRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
    }

    @Override
    public VenueResponse findByCode(String code) {
        return venueRepository.findByCode(code)
                .map(venueMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + code));
    }

    @Override
    public List<VenueResponse> findActiveVenues() {
        return venueRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(venueMapper::toResponse).toList();
    }
}
