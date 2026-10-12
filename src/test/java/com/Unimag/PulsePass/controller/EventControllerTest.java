package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.domain.enums.EventCategory;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.dto.request.CreateEventRequest;
import com.Unimag.PulsePass.dto.response.ArtistResponse;
import com.Unimag.PulsePass.dto.response.EventResponse;
import com.Unimag.PulsePass.dto.response.EventSummaryResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.service.EventService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void create_validRequest_returns201() throws Exception {
        when(eventService.create(any(CreateEventRequest.class)))
                .thenReturn(eventResponse(EventStatus.DRAFT));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequest()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2030"))
                .andExpect(jsonPath("$.name")
                        .value("Caribbean Music Fest"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(eventService).create(any(CreateEventRequest.class));
    }

    @Test
    void create_invalidRequest_returns400() throws Exception {
        String invalidRequest = """
                {
                  "eventCode": "",
                  "name": "",
                  "description": "Festival",
                  "category": "MUSIC",
                  "eventDate": "2030-06-15T20:00:00",
                  "minimumAge": 18,
                  "venueCode": ""
                }
                """;

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.details.eventCode").exists())
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.venueCode").exists());

        verify(eventService, never())
                .create(any(CreateEventRequest.class));
    }

    @Test
    void findByCode_existingEvent_returns200() throws Exception {
        when(eventService.findByCode("CMF-2030"))
                .thenReturn(eventResponse(EventStatus.DRAFT));

        mockMvc.perform(get("/api/events/CMF-2030"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2030"))
                .andExpect(jsonPath("$.name")
                        .value("Caribbean Music Fest"));

        verify(eventService).findByCode("CMF-2030");
    }

    @Test
    void findByCode_nonExistingEvent_returns404() throws Exception {
        when(eventService.findByCode("NOT-FOUND"))
                .thenThrow(new ResourceNotFoundException(
                        "Event not found: NOT-FOUND"));

        mockMvc.perform(get("/api/events/NOT-FOUND"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Event not found: NOT-FOUND"))
                .andExpect(jsonPath("$.details").isMap());

        verify(eventService).findByCode("NOT-FOUND");
    }

    @Test
    void findPublishedEvents_returns200WithList() throws Exception {
        when(eventService.findPublishedEvents())
                .thenReturn(List.of(eventSummary(EventStatus.PUBLISHED)));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2030"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        verify(eventService).findPublishedEvents();
    }

    @Test
    void publish_validEvent_returns200() throws Exception {
        when(eventService.publish("CMF-2030"))
                .thenReturn(eventResponse(EventStatus.PUBLISHED));

        mockMvc.perform(patch("/api/events/CMF-2030/publish"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2030"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2030");
    }

    @Test
    void publish_invalidTransition_returns409() throws Exception {
        when(eventService.publish("CMF-2030"))
                .thenThrow(new BusinessRuleException(
                        "Event cannot be published"));

        mockMvc.perform(patch("/api/events/CMF-2030/publish"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Event cannot be published"));

        verify(eventService).publish("CMF-2030");
    }

    @Test
    void addArtist_existingArtist_returns200() throws Exception {
        when(eventService.addArtist("CMF-2030", 1L))
                .thenReturn(eventResponse(EventStatus.DRAFT));

        mockMvc.perform(post("/api/events/CMF-2030/artists/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2030"));

        verify(eventService).addArtist("CMF-2030", 1L);
    }

    @Test
    void findByArtist_returns200WithList() throws Exception {
        when(eventService.findByArtist("Solar Beat"))
                .thenReturn(List.of(eventSummary(EventStatus.PUBLISHED)));

        mockMvc.perform(get("/api/events/by-artist")
                        .param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2030"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        verify(eventService).findByArtist("Solar Beat");
    }

    private String validCreateRequest() {
        return """
                {
                  "eventCode": "CMF-2030",
                  "name": "Caribbean Music Fest",
                  "description": "Music festival",
                  "category": "MUSIC",
                  "eventDate": "2030-06-15T20:00:00",
                  "minimumAge": 18,
                  "venueCode": "VEN-SMR-01"
                }
                """;
    }

    private EventResponse eventResponse(EventStatus status) {
        return new EventResponse(
                1L,
                "CMF-2030",
                "Caribbean Music Fest",
                "Music festival",
                EventCategory.MUSIC,
                status,
                LocalDateTime.of(2030, 6, 15, 20, 0),
                18,
                "VEN-SMR-01",
                "Marina Convention Center",
                Set.of(new ArtistResponse(
                        1L, "Solar Beat", "Colombia", "Electronic", true))
        );
    }

    private EventSummaryResponse eventSummary(EventStatus status) {
        return new EventSummaryResponse(
                1L,
                "CMF-2030",
                "Caribbean Music Fest",
                EventCategory.MUSIC,
                status,
                LocalDateTime.of(2030, 6, 15, 20, 0),
                "Marina Convention Center",
                "Santa Marta"
        );
    }
}
