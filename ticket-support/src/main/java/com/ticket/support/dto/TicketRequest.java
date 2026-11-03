package com.ticket.support.dto;

import com.ticket.support.entity.Category;
import com.ticket.support.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TicketRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username cannot exceed 100 characters")
        String username,

        @NotBlank(message = "Summary is required")
        @Size(max = 100, message = "Summary cannot exceed 100 characters")
        String summary,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Priority is required")
        Priority priority,

        @NotNull(message = "Category is required")
        Category category
) {
}