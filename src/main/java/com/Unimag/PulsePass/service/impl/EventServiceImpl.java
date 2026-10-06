package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.Artist;
import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.domain.Venue;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.dto.request.CreateEventRequest;
import com.Unimag.PulsePass.dto.response.EventResponse;
import com.Unimag.PulsePass.dto.response.EventSummaryResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.DuplicateResourceException;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.mapper.EventMapper;
import com.Unimag.PulsePass.repository.ArtistRepository;
import com.Unimag.PulsePass.repository.EventRepository;
import com.Unimag.PulsePass.repository.VenueRepository;
import com.Unimag.PulsePass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, VenueRepository venueRepository,
                            ArtistRepository artistRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {
        if (request == null) throw new BusinessRuleException("Event request is required.");
        if (request.eventCode() == null || request.eventCode().isBlank())
            throw new BusinessRuleException("Event code is required.");
        if (request.name() == null || request.name().isBlank())
            throw new BusinessRuleException("Event name is required.");
        if (request.venueCode() == null || request.venueCode().isBlank())
            throw new BusinessRuleException("Venue code is required.");
        if (eventRepository.existsByEventCode(request.eventCode()))
            throw new DuplicateResourceException("Event code already exists: " + request.eventCode());
        if (request.eventDate() == null || !request.eventDate().isAfter(LocalDateTime.now()))
            throw new BusinessRuleException("Event date must be in the future.");
        if (request.minimumAge() == null || request.minimumAge() < 0)
            throw new BusinessRuleException("Minimum age must be zero or greater.");
        if (request.category() == null) throw new BusinessRuleException("Event category is required.");

        Venue venue = venueRepository.findByCode(request.venueCode())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + request.venueCode()));
        if (!Boolean.TRUE.equals(venue.getActive()))
            throw new BusinessRuleException("Cannot create an event at inactive venue: " + venue.getCode());

        Event event = new Event();
        event.setEventCode(request.eventCode());
        event.setName(request.name());
        event.setDescription(request.description());
        event.setCategory(request.category());
        event.setEventDate(request.eventDate());
        event.setMinimumAge(request.minimumAge());
        event.setVenue(venue);
        event.setStatus(EventStatus.DRAFT);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse findByCode(String eventCode) {
        return eventRepository.findByEventCode(eventCode).map(eventMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventCode));
    }

    @Override
    public List<EventSummaryResponse> findPublishedEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED).stream()
                .map(eventMapper::toSummary).toList();
    }

    @Override
    @Transactional
    public EventResponse publish(String eventCode) {
        Event event = getEvent(eventCode);
        if (event.getStatus() != EventStatus.DRAFT)
            throw new BusinessRuleException("Only DRAFT events can be published: " + eventCode);
        if (!event.getEventDate().isAfter(LocalDateTime.now()))
            throw new BusinessRuleException("Event date must be in the future: " + eventCode);
        if (!Boolean.TRUE.equals(event.getVenue().getActive()))
            throw new BusinessRuleException("Cannot publish an event at an inactive venue: " + event.getVenue().getCode());
        event.setStatus(EventStatus.PUBLISHED);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse addArtist(String eventCode, Long artistId) {
        Event event = getEvent(eventCode);
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED)
            throw new BusinessRuleException("Artists cannot be added to " + event.getStatus() + " events.");
        if (event.getArtists().stream().anyMatch(existing -> existing.getId().equals(artist.getId())))
            throw new DuplicateResourceException("Artist is already associated with event: " + eventCode);
        event.getArtists().add(artist);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public List<EventSummaryResponse> findByArtist(String stageName) {
        return eventRepository.findEventsByArtistStageName(stageName).stream()
                .map(eventMapper::toSummary).toList();
    }

    private Event getEvent(String eventCode) {
        return eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventCode));
    }
}
