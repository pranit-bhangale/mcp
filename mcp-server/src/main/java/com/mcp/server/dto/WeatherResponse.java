package com.mcp.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResponse(
        String timezone,
        CurrentWeather current
) {

    public record CurrentWeather(
            String time,

            @JsonProperty("temperature_2m")
            Double temperature,

            @JsonProperty("relative_humidity_2m")
            Integer humidity,

            @JsonProperty("apparent_temperature")
            Double feelsLike,

            Double precipitation,

            @JsonProperty("weather_code")
            Integer weatherCode,

            @JsonProperty("wind_speed_10m")
            Double windSpeed,

            @JsonProperty("wind_direction_10m")
            Double windDirection,

            @JsonProperty("is_day")
            Integer isDay
    ) {
    }
}
