package com.ticket.support.dto;

import lombok.Builder;

@Builder
public record AgentResponse(
        String intent,
        String message,
        TicketResponse response
) {
}
