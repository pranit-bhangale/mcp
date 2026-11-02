package com.ticket.support.service;

import com.ticket.support.dto.TicketRequest;
import com.ticket.support.dto.TicketResponse;

public interface TicketService {

    TicketResponse createTicket(TicketRequest request);

    TicketResponse fetchTicketDetailById(String ticketId);
}
