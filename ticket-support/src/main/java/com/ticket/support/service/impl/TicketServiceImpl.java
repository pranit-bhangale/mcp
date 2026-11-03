package com.ticket.support.service.impl;

import com.ticket.support.dto.TicketRequest;
import com.ticket.support.dto.TicketResponse;
import com.ticket.support.dto.UpdateRequest;
import com.ticket.support.entity.Status;
import com.ticket.support.entity.Ticket;
import com.ticket.support.page.service.Page;
import com.ticket.support.page.wrapper.PageResponse;
import com.ticket.support.repository.TicketRepository;
import com.ticket.support.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "createdAt", "updatedAt", "ticketId", "username", "summary", "priority", "category", "status"
    );

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
        return toTicketResponse(ticket);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = Exception.class)
    public TicketResponse fetchTicketDetailById(final String ticketId) {
        final Ticket ticket = this.ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        return toTicketResponse(ticket);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public TicketResponse updateTicket(final String ticketId, final UpdateRequest request) {
        final Ticket ticket = this.ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        final Status currentStatus = ticket.getStatus();
        final Status nextStatus = switch (currentStatus) {
            case PENDING -> Status.OPEN;
            case OPEN -> Status.IN_PROGRESS;
            case IN_PROGRESS -> Status.DONE;
            case DONE -> Status.RESOLVED;
            case RESOLVED -> Status.CLOSED;
            case CLOSED -> throw new IllegalStateException("Ticket is already closed and cannot be updated.");
        };
        ticket.setStatus(nextStatus);
        return toTicketResponse(ticket);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteTicket(final String ticketId) {
        final Ticket ticket = this.ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        this.ticketRepository.delete(ticket);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = Exception.class)
    public PageResponse<TicketResponse> getTickets(
            final int page, final int size, final String keyword,
            final String sortBy, final String sortDirection
    ) {
        Specification<Ticket> specification = Specification.unrestricted();
        if (StringUtils.hasText(keyword)) {
            final String pattern = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, _, cb) -> cb.or(
                    cb.like(cb.lower(root.get("ticketId")), pattern),
                    cb.like(cb.lower(root.get("summary")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("username")), pattern)));
        }
        return Page.paginate(this.ticketRepository, specification, page, size, sortBy, sortDirection, ALLOWED_SORT_PROPERTIES, this::toTicketResponse);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = Exception.class)
    public Status getTicketStatus(final String ticketId) {
        return this.ticketRepository.findStatusOfTicket(ticketId);
    }

    private String generateTicketId() {
        return "TKT-" + (100000 + new SecureRandom().nextInt(900000));
    }

    private TicketResponse toTicketResponse(final Ticket ticket) {
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
}
