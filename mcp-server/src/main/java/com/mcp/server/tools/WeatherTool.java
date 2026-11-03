package com.mcp.server.tools;

import com.mcp.server.dto.WeatherDetail;
import com.mcp.server.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherTool {

    private final WeatherService weatherService;

    @McpTool(name = "get_weather_by_city", description = """
            Retrieves current weather conditions for a specified city anywhere
            in the world using the Open-Meteo Weather API.
            
            Returns:
            - City and country information
            - State or region
            - Local timezone
            - Weather observation time
            - Current temperature in Celsius
            - Feels-like temperature in Celsius
            - Relative humidity percentage
            - Wind speed in km/h
            - Wind direction in degrees
            - Precipitation in millimeters
            - Weather condition description
            - Weather code
            - Day or night status
            
            Use this tool whenever the user asks about current weather,
            temperature, humidity, rain, wind, or weather conditions
            for a particular location.
            """
    )
    public WeatherDetail getWeatherByCity(
            @McpToolParam(description = """
                    Name of the city or location for which current weather
                    information is required.
                    
                    Examples:
                    - Pune
                    - Mumbai
                    - London
                    - New York
                    - Delhi
                    
                    Provide a valid city name. Do not provide latitude
                    or longitude. The tool automatically resolves the
                    location coordinates using the Geocoding API.
                    
                    If multiple locations have the same name, ask the
                    user to specify the country or state.
                    """,
                    required = true
            )
            String city
    ) {
        return this.weatherService.getWeather(city);
    }
}
