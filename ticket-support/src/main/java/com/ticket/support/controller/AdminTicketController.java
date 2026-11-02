package com.ticket.support.controller;

import com.ticket.support.dto.TicketResponse;
import com.ticket.support.dto.UpdateRequest;
import com.ticket.support.page.wrapper.PageResponse;
import com.ticket.support.service.TicketService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@Validated
public class AdminTicketController {

    private final TicketService ticketService;

    @GetMapping(version = "v1")
    public ResponseEntity<PageResponse<TicketResponse>> getTickets(
            @RequestParam(defaultValue = "0") @Min(0) final int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) final int size,
            @RequestParam(required = false) final String keyword,
            @RequestParam(defaultValue = "createdAt") final String sortBy,
            @RequestParam(defaultValue = "desc") final String sortDirection
    ) {
        final PageResponse<TicketResponse> response = this.ticketService.getTickets(
                page, size, keyword, sortBy, sortDirection
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{ticketId}", version = "v1")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable final String ticketId) {
        final TicketResponse response = this.ticketService.fetchTicketDetailById(ticketId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{ticketId}/status", version = "v1")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable final String ticketId,
            @RequestBody(required = false) final UpdateRequest request
    ) {
        final TicketResponse response = this.ticketService.updateTicket(
                ticketId, request != null ? request : new UpdateRequest(null)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{ticketId}", version = "v1")
    public ResponseEntity<Void> deleteTicket(@PathVariable final String ticketId) {
        this.ticketService.deleteTicket(ticketId);
        return ResponseEntity.noContent().build();
    }
}
