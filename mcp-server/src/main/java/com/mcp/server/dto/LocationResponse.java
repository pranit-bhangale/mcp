package com.mcp.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LocationResponse(
        List<Location> results
) {

    public record Location(
            String name,
            String country,
            @JsonProperty("admin1")
            String state,
            Double latitude,
            Double longitude,
            String timezone
    ) {
    }
}