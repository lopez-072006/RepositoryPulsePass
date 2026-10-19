
package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.dto.response.VenueResponse;
import com.Unimag.PulsePass.service.VenueService;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.Unimag.PulsePass.security.SecurityConfig;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
@Import({SecurityConfig.class, SecurityMvcTestConfiguration.class})
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @Test
void findByCode_nonExistingVenue_returns404() throws Exception {

    when(venueService.findByCode("VEN-999"))
            .thenThrow(
                    new ResourceNotFoundException(
                            "Venue not found: VEN-999"
                    )
            );

    mockMvc.perform(get("/api/venues/VEN-999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(
                    MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message")
                    .value("Venue not found: VEN-999"))
            .andExpect(jsonPath("$.details").isMap());

    verify(venueService).findByCode("VEN-999");
}

    @Test
    void findByCode_existingVenue_returns200() throws Exception {
        VenueResponse venue = new VenueResponse(
                1L,
                "VEN-SMR-01",
                "Marina Convention Center",
                "Santa Marta",
                "Carrera 1",
                3,
                true
        );

        when(venueService.findByCode("VEN-SMR-01"))
                .thenReturn(venue);

        mockMvc.perform(get("/api/venues/VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name")
                        .value("Marina Convention Center"))
                .andExpect(jsonPath("$.city").value("Santa Marta"))
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.active").value(true));

        verify(venueService).findByCode("VEN-SMR-01");
    }

    @Test
    void findActiveVenues_returns200WithList() throws Exception {
        VenueResponse venue = new VenueResponse(
                1L,
                "VEN-SMR-01",
                "Marina Convention Center",
                "Santa Marta",
                "Carrera 1",
                3,
                true
        );

        when(venueService.findActiveVenues())
                .thenReturn(List.of(venue));

        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(venueService).findActiveVenues();
    }
}
