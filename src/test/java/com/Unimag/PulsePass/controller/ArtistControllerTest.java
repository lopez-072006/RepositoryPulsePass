package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.dto.response.ArtistResponse;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.service.ArtistService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArtistController.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService artistService;

    @Test
    void findById_existingArtist_returns200() throws Exception {
        ArtistResponse artist = new ArtistResponse(
                1L, "Solar Beat", "Colombia", "Electronic", true
        );

        when(artistService.findById(1L)).thenReturn(artist);

        mockMvc.perform(get("/api/artists/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"))
                .andExpect(jsonPath("$.country").value("Colombia"))
                .andExpect(jsonPath("$.genre").value("Electronic"))
                .andExpect(jsonPath("$.active").value(true));

        verify(artistService).findById(1L);
    }

    @Test
    void findById_nonExistingArtist_returns404() throws Exception {
        when(artistService.findById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Artist not found: 999"));

        mockMvc.perform(get("/api/artists/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Artist not found: 999"))
                .andExpect(jsonPath("$.details").isMap());

        verify(artistService).findById(999L);
    }

    @Test
    void findByStageName_existingArtist_returns200() throws Exception {
        ArtistResponse artist = new ArtistResponse(
                1L, "Solar Beat", "Colombia", "Electronic", true
        );

        when(artistService.findByStageName("Solar Beat"))
                .thenReturn(artist);

        mockMvc.perform(get("/api/artists/by-stage-name")
                        .param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(artistService).findByStageName("Solar Beat");
    }

    @Test
    void findActiveArtists_returns200WithList() throws Exception {
        ArtistResponse artist = new ArtistResponse(
                1L, "Solar Beat", "Colombia", "Electronic", true
        );

        when(artistService.findActiveArtists())
                .thenReturn(List.of(artist));

        mockMvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].stageName")
                        .value("Solar Beat"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(artistService).findActiveArtists();
    }
}