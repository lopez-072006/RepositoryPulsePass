package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.domain.enums.TicketStatus;
import com.Unimag.PulsePass.domain.enums.TicketType;
import com.Unimag.PulsePass.dto.request.PurchaseTicketRequest;
import com.Unimag.PulsePass.dto.response.TicketResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.GlobalExceptionHandler;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.service.TicketService;
import com.Unimag.PulsePass.security.SecurityConfig;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({SecurityConfig.class, SecurityMvcTestConfiguration.class, GlobalExceptionHandler.class})
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    private PurchaseTicketRequest validRequest() {
        return new PurchaseTicketRequest(
                "user@example.com",
                "EVT-001",
                TicketType.values()[0]
        );
    }

    private TicketResponse ticketResponse() {
        return new TicketResponse(
                1L,
                "TKT-001",
                TicketType.values()[0],
                new BigDecimal("100000.00"),
                TicketStatus.values()[0],
                LocalDateTime.of(2026, 10, 10, 20, 0),
                "user@example.com",
                "EVT-001",
                "Festival de prueba"
        );
    }

    
    @Test
    @WithMockUser(roles = "USER")
    void purchase_whenRequestIsValid_returnsCreated() throws Exception {
        given(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .willReturn(ticketResponse());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(validRequest())
                        ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"));
    }

    
    @Test
    @WithMockUser(roles = "USER")
    void purchase_whenRequestIsInvalid_returnsBadRequest() throws Exception {
        String invalidRequest = """
                {
                    "userEmail": "",
                    "eventCode": "",
                    "type": null
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(ticketService, never())
                .purchase(any(PurchaseTicketRequest.class));
    }

    
    @Test
    @WithMockUser(roles = "USER")
    void purchase_whenUserDoesNotExist_returnsNotFound() throws Exception {
        given(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .willThrow(
                        new ResourceNotFoundException("User not found")
                );

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(validRequest())
                        ))
                .andExpect(status().isNotFound());
    }

   
    @Test
    @WithMockUser(roles = "USER")
    void purchase_whenBusinessRuleIsViolated_returnsConflict()
            throws Exception {

        given(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .willThrow(
                        new BusinessRuleException(
                                "Ticket purchase is not allowed"
                        )
                );

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(validRequest())
                        ))
                .andExpect(status().isConflict());
    }

    
    @Test
    @WithMockUser(roles = "USER")
    void findByCode_whenTicketExists_returnsOk() throws Exception {
        given(ticketService.findByCode("TKT-001"))
                .willReturn(ticketResponse());

        mockMvc.perform(get("/api/tickets/TKT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"));
    }

    
    @Test
    @WithMockUser(roles = "USER")
    void findByUserEmail_returnsTickets() throws Exception {
        given(ticketService.findByUserEmail("user@example.com"))
                .willReturn(List.of(ticketResponse()));

        mockMvc.perform(get("/api/tickets/by-user")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].ticketCode").value("TKT-001")
                );
    }

    
    @Test
    @WithMockUser(roles = "STAFF")
    void findPaidTicketsByEvent_returnsTickets() throws Exception {
        given(ticketService.findPaidTicketsByEvent("EVT-001"))
                .willReturn(List.of(ticketResponse()));

        mockMvc.perform(
                        get("/api/events/EVT-001/tickets/paid")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].eventCode").value("EVT-001")
                );
    }

    
    @Test
    @WithMockUser(roles = "USER")
    void cancel_whenOperationIsValid_returnsOk() throws Exception {
        given(ticketService.cancel("TKT-001"))
                .willReturn(ticketResponse());

        mockMvc.perform(patch("/api/tickets/TKT-001/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"));
    }

    
    @Test
    @WithMockUser(roles = "USER")
    void cancel_whenBusinessRuleIsViolated_returnsConflict()
            throws Exception {

        given(ticketService.cancel("TKT-001"))
                .willThrow(
                        new BusinessRuleException(
                                "Ticket cannot be cancelled"
                        )
                );

        mockMvc.perform(patch("/api/tickets/TKT-001/cancel"))
                .andExpect(status().isConflict());
    }

    
    @Test
    @WithMockUser(roles = "STAFF")
    void markAsUsed_whenOperationIsValid_returnsOk() throws Exception {
        given(ticketService.markAsUsed("TKT-001"))
                .willReturn(ticketResponse());

        mockMvc.perform(patch("/api/tickets/TKT-001/use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotMarkTicketAsUsed() throws Exception {
        mockMvc.perform(patch("/api/tickets/TKT-001/use"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(ticketService, never()).markAsUsed("TKT-001");
    }

    
    @Test
    @WithMockUser(roles = "STAFF")
    void markAsUsed_whenBusinessRuleIsViolated_returnsConflict()
            throws Exception {

        given(ticketService.markAsUsed("TKT-001"))
                .willThrow(
                        new BusinessRuleException(
                                "Ticket cannot be used"
                        )
                );

        mockMvc.perform(patch("/api/tickets/TKT-001/use"))
                .andExpect(status().isConflict());
    }
}
