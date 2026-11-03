package com.mcp.client.dto;

import lombok.Builder;

@Builder
public record AgentResponse(
        String content
) {
}
