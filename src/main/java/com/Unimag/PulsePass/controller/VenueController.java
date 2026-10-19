package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.dto.response.VenueResponse;
import com.Unimag.PulsePass.service.VenueService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // Consultar un venue por su código
    @GetMapping("/{code}")
    public ResponseEntity<VenueResponse> findByCode(
            @PathVariable String code) {

        VenueResponse venue = venueService.findByCode(code);

        return ResponseEntity.ok(venue);
    }

    // Consultar todos los venues activos
    @GetMapping("/active")
    public ResponseEntity<List<VenueResponse>> findActiveVenues() {

        List<VenueResponse> venues = venueService.findActiveVenues();

        return ResponseEntity.ok(venues);
    }
}
