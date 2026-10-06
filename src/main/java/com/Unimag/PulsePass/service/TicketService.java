package com.Unimag.PulsePass.service;

import com.Unimag.PulsePass.dto.request.PurchaseTicketRequest;
import com.Unimag.PulsePass.dto.response.TicketResponse;
import java.util.List;

public interface TicketService {
    TicketResponse purchase(PurchaseTicketRequest request);
    TicketResponse findByCode(String ticketCode);
    List<TicketResponse> findByUserEmail(String email);
    List<TicketResponse> findPaidTicketsByEvent(String eventCode);
    TicketResponse cancel(String ticketCode);
    TicketResponse markAsUsed(String ticketCode);
}
