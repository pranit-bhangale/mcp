package com.ticket.support.service;

import com.ticket.support.dto.TicketRequest;
import com.ticket.support.dto.TicketResponse;
import com.ticket.support.dto.UpdateRequest;

public interface TicketService {

    TicketResponse createTicket(TicketRequest request);

    TicketResponse fetchTicketDetailById(String ticketId);

    TicketResponse updateTicket(String ticketId, UpdateRequest request);
}
