package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.dto.request.PurchaseTicketRequest;
import com.Unimag.PulsePass.dto.response.TicketResponse;
import com.Unimag.PulsePass.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    
    @PostMapping("/api/tickets")
    public ResponseEntity<TicketResponse> purchase(
            @Valid @RequestBody PurchaseTicketRequest request) {

        TicketResponse response = ticketService.purchase(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    
    @GetMapping("/api/tickets/{ticketCode}")
    public ResponseEntity<TicketResponse> findByCode(
            @PathVariable String ticketCode) {

        return ResponseEntity.ok(ticketService.findByCode(ticketCode));
    }

    
    @GetMapping("/api/tickets/by-user")
    public ResponseEntity<List<TicketResponse>> findByUserEmail(
            @RequestParam("email") String email) {

        return ResponseEntity.ok(ticketService.findByUserEmail(email));
    }

    
    @GetMapping("/api/events/{eventCode}/tickets/paid")
    public ResponseEntity<List<TicketResponse>> findPaidTicketsByEvent(
            @PathVariable String eventCode) {

        return ResponseEntity.ok(
                ticketService.findPaidTicketsByEvent(eventCode));
    }

    
    @PatchMapping("/api/tickets/{ticketCode}/cancel")
    public ResponseEntity<TicketResponse> cancel(
            @PathVariable String ticketCode) {

        return ResponseEntity.ok(ticketService.cancel(ticketCode));
    }

    
    @PatchMapping("/api/tickets/{ticketCode}/use")
    public ResponseEntity<TicketResponse> markAsUsed(
            @PathVariable String ticketCode) {

        return ResponseEntity.ok(ticketService.markAsUsed(ticketCode));
    }
}
