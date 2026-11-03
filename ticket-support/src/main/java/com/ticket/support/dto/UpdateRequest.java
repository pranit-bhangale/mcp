package com.ticket.support.dto;

import com.ticket.support.entity.Status;

public record UpdateRequest(
        Status status
) {
}