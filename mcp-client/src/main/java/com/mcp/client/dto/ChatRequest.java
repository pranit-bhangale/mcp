package com.mcp.client.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "Prompt required")
        String prompt
) {
}
