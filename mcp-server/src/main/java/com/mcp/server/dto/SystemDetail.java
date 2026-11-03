package com.mcp.server.dto;

import lombok.Builder;

@Builder
public record SystemDetail(
        String systemName,
        String systemType,
        String systemVersion,
        String manufacturer,
        String model,
        String hostname,
        String operatingSystem,
        String osVersion,
        String architecture,
        String description
) {
}