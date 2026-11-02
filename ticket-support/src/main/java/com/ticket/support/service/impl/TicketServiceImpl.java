package com.ticket.support.service.impl;

import com.ticket.support.dto.TicketRequest;
import com.ticket.support.dto.TicketResponse;
import com.ticket.support.dto.UpdateRequest;
import com.ticket.support.entity.Status;
import com.ticket.support.entity.Ticket;
import com.ticket.support.repository.TicketRepository;
import com.ticket.support.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TicketResponse createTicket(final TicketRequest request) {
        final boolean exists = this.ticketRepository.existsBySummaryAndUsernameAndUserId
                (request.summary(), request.username(), request.userId());
        if (exists) {
            throw new RuntimeException("Ticket with this summary already exists");
        }
        Ticket ticket = Ticket.builder()
                .ticketId(generateTicketId())
                .userId(request.userId())
                .username(request.username())
                .summary(request.summary())
                .description(request.description())
                .priority(request.priority())
                .category(request.category())
                .build();
        ticket = this.ticketRepository.save(ticket);
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUserId())
                .username(ticket.getUsername())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = Exception.class)
    public TicketResponse fetchTicketDetailById(final String ticketId) {
        final Ticket ticket = this.ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUserId())
                .username(ticket.getUsername())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TicketResponse updateTicket(final String ticketId, final UpdateRequest request) {
        final Ticket ticket = this.ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        final Status currentStatus = ticket.getStatus();
        final Status nextStatus = switch (currentStatus) {
            case PENDING -> Status.OPEN;
            case OPEN -> Status.DONE;
            case DONE -> Status.CLOSED;
            case CLOSED -> throw new IllegalStateException("Ticket is already closed and cannot be updated.");
        };
        ticket.setStatus(nextStatus);
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUserId())
                .username(ticket.getUsername())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private String generateTicketId() {
        return "TKT-" + (100000 + new SecureRandom().nextInt(900000));
    }
}
