package com.mcp.server.service;

import com.mcp.server.dto.LocationResponse;
import com.mcp.server.dto.WeatherDetail;
import com.mcp.server.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com";
    private final RestClient restClient;

    public WeatherDetail getWeather(String city) {
        LocationResponse locationResponse = restClient.get()
                .uri(GEOCODING_URL + "/v1/search?name={city}&count=1&language=en&format=json", city)
                .retrieve()
                .body(LocationResponse.class);
        if (locationResponse == null || locationResponse.results() == null || locationResponse.results().isEmpty()) {
            throw new IllegalArgumentException("Location not found: " + city);
        }
        LocationResponse.Location location = locationResponse.results().getFirst();
        WeatherResponse weatherResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.open-meteo.com")
                        .path("/v1/forecast")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .queryParam("current",
                                "temperature_2m," +
                                        "relative_humidity_2m," +
                                        "apparent_temperature," +
                                        "precipitation," +
                                        "weather_code," +
                                        "wind_speed_10m," +
                                        "wind_direction_10m," +
                                        "is_day")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(WeatherResponse.class);
        if (weatherResponse == null || weatherResponse.current() == null) {
            throw new IllegalStateException("Weather information unavailable");
        }
        WeatherResponse.CurrentWeather current = weatherResponse.current();
        return WeatherDetail.builder()
                .city(location.name())
                .country(location.country())
                .state(location.state())
                .timezone(weatherResponse.timezone())
                .observationTime(current.time())
                .temperature(current.temperature())
                .feelsLike(current.feelsLike())
                .humidity(current.humidity())
                .windSpeed(current.windSpeed())
                .windDirection(current.windDirection())
                .precipitation(current.precipitation())
                .weatherCode(current.weatherCode())
                .weatherCondition(getWeatherCondition(current.weatherCode()))
                .isDay(current.isDay() != null && current.isDay() == 1)
                .build();
    }

    private String getWeatherCondition(Integer code) {
        if (code == null) return "Unknown";
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75 -> "Snowfall";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown";
        };
    }
}
