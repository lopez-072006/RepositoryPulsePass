package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.Artist;
import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.domain.Venue;
import com.Unimag.PulsePass.domain.enums.EventCategory;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.dto.request.CreateEventRequest;
import com.Unimag.PulsePass.dto.response.EventResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.DuplicateResourceException;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.mapper.EventMapper;
import com.Unimag.PulsePass.repository.ArtistRepository;
import com.Unimag.PulsePass.repository.EventRepository;
import com.Unimag.PulsePass.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock EventRepository eventRepository;
    @Mock VenueRepository venueRepository;
    @Mock ArtistRepository artistRepository;
    @Mock EventMapper eventMapper;
    @InjectMocks EventServiceImpl service;
    private Venue activeVenue;
    private EventResponse response;

    @BeforeEach
    void setUp() {
        activeVenue = new Venue();
        activeVenue.setId(1L);
        activeVenue.setCode("VEN-1");
        activeVenue.setName("Venue");
        activeVenue.setActive(true);
        response = new EventResponse(1L, "EVT-1", "Concert", null, EventCategory.MUSIC,
                EventStatus.DRAFT, LocalDateTime.now().plusDays(30), 18, "VEN-1", "Venue", Set.of());
    }

    @Test
    void findByCode_whenEventExists_returnsDto() {
        Event event = event(EventStatus.DRAFT);
        when(eventRepository.findByEventCode("EVT-1")).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(response);
        assertThat(service.findByCode("EVT-1")).isEqualTo(response);
    }

    @Test
    void findByCode_whenEventDoesNotExist_throwsNotFound() {
        when(eventRepository.findByEventCode("MISSING")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByCode("MISSING")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validRequest_savesDraftEvent() {
        when(eventRepository.existsByEventCode("EVT-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(activeVenue));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventMapper.toResponse(any(Event.class))).thenReturn(response);
        assertThat(service.create(request(LocalDateTime.now().plusDays(30)))).isEqualTo(response);
        verify(eventRepository).save(any(Event.class));
        verify(eventMapper).toResponse(any(Event.class));
    }

    @Test
    void create_duplicateCode_throwsAndDoesNotSave() {
        when(eventRepository.existsByEventCode("EVT-1")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request(LocalDateTime.now().plusDays(30))))
                .isInstanceOf(DuplicateResourceException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void create_whenVenueDoesNotExist_throwsAndDoesNotSave() {
        when(eventRepository.existsByEventCode("EVT-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request(LocalDateTime.now().plusDays(30))))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void create_whenVenueIsInactive_throwsAndDoesNotSave() {
        activeVenue.setActive(false);
        when(eventRepository.existsByEventCode("EVT-1")).thenReturn(false);
        when(venueRepository.findByCode("VEN-1")).thenReturn(Optional.of(activeVenue));
        assertThatThrownBy(() -> service.create(request(LocalDateTime.now().plusDays(30))))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void create_whenDateIsInPast_throwsAndDoesNotSave() {
        when(eventRepository.existsByEventCode("EVT-1")).thenReturn(false);
        assertThatThrownBy(() -> service.create(request(LocalDateTime.now().minusDays(1))))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publish_draftEventWithActiveVenue_changesStatusToPublished() {
        Event event = event(EventStatus.DRAFT);
        when(eventRepository.findByEventCode("EVT-1")).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);
        service.publish("EVT-1");
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test
    void publish_cancelledEvent_throwsAndDoesNotSave() {
        when(eventRepository.findByEventCode("EVT-1")).thenReturn(Optional.of(event(EventStatus.CANCELLED)));
        assertThatThrownBy(() -> service.publish("EVT-1")).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtist_associatesArtistToEvent() {
        Event event = event(EventStatus.DRAFT);
        Artist artist = new Artist();
        artist.setId(2L);
        when(eventRepository.findByEventCode("EVT-1")).thenReturn(Optional.of(event));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);
        service.addArtist("EVT-1", 2L);
        assertThat(event.getArtists()).contains(artist);
        verify(eventRepository).save(event);
    }

    @Test
    void addArtist_whenArtistAlreadyAssociated_throwsAndDoesNotSave() {
        Event event = event(EventStatus.DRAFT);
        Artist artist = new Artist();
        artist.setId(2L);
        event.setArtists(Set.of(artist));
        when(eventRepository.findByEventCode("EVT-1")).thenReturn(Optional.of(event));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(artist));
        assertThatThrownBy(() -> service.addArtist("EVT-1", 2L)).isInstanceOf(DuplicateResourceException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    private Event event(EventStatus status) {
        Event event = new Event();
        event.setId(1L);
        event.setEventCode("EVT-1");
        event.setName("Concert");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(status);
        event.setEventDate(LocalDateTime.now().plusDays(30));
        event.setMinimumAge(18);
        event.setVenue(activeVenue);
        return event;
    }

    private CreateEventRequest request(LocalDateTime date) {
        return new CreateEventRequest("EVT-1", "Concert", null, EventCategory.MUSIC, date, 18, "VEN-1");
    }
}
