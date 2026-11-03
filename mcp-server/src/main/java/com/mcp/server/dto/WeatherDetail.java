package com.mcp.server.dto;

import lombok.Builder;

@Builder
public record WeatherDetail(
        String city,
        String country,
        String state,
        String timezone,
        String observationTime,
        Double temperature,
        Double feelsLike,
        Integer humidity,
        Double windSpeed,
        Double windDirection,
        Double precipitation,
        Integer weatherCode,
        String weatherCondition,
        Boolean isDay
) {
}
