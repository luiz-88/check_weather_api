package com.example.check_weather_api.controller;

import com.example.check_weather_api.service.CheckWeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class CheckWeatherController {
    private final CheckWeatherService checkWeatherService;

    public CheckWeatherController(CheckWeatherService checkWeatherService) {
        this.checkWeatherService = checkWeatherService;
    }

    @GetMapping
    public ResponseEntity<?> getWeather(@RequestParam String city,
                                        @RequestParam String country,
                                        @RequestParam String apiKey) {
        try {
            String description = checkWeatherService.getWeatherDescription(city, country, apiKey);
            return ResponseEntity.ok(description);
        } catch (Exception e) {
            return ResponseEntity.status(429).body(e.getMessage());
        }
    }
}

