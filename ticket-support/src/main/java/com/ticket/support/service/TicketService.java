package com.ticket.support.service;

import com.ticket.support.dto.TicketRequest;
import com.ticket.support.dto.TicketResponse;
import com.ticket.support.dto.UpdateRequest;
import com.ticket.support.entity.Status;
import com.ticket.support.page.wrapper.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public interface TicketService {

    TicketResponse createTicket(TicketRequest request);

    TicketResponse fetchTicketDetailById(String ticketId);

    TicketResponse updateTicket(String ticketId, UpdateRequest request);

    void deleteTicket(String ticketId);

    PageResponse<TicketResponse> getTickets
            (@Min(0) int page, @Min(1) @Max(50) int size, String keyword, String sortBy, String sortDirection);

    Status getTicketStatus(String ticketId);
}
