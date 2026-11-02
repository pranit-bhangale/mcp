package com.ticket.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ticket.support.entity.Category;
import com.ticket.support.entity.Priority;
import com.ticket.support.entity.Status;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketResponse(
        UUID id,
        String ticketId,
        UUID userId,
        String username,
        String description,
        Priority priority,
        Category category,
        Status status,
        Instant createdAt,
        Instant updatedAt
) {
}
