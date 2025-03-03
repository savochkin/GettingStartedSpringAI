package org.example;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherService {
    private final Map<String, String> cityWeather = new HashMap<>();

    public WeatherService() {
        cityWeather.put("Paris", "21°C, Light Rain");
        cityWeather.put("London", "18°C, Foggy");
        cityWeather.put("New York", "25°C, Sunny");
    }

    @Tool(description = "get current weather for a city")
    public String getWeatherForCity(String city) {
        return cityWeather.getOrDefault(city, "I do not know the weather for " + city);
    }
}